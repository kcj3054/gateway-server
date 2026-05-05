# Gateway Server Project

Spring Boot 기반의 인증 서버와 Spring Cloud Gateway 서버를 분리해 구성한 API Gateway 실습 프로젝트입니다.  
외부 요청은 Gateway를 통해 내부 서비스로 라우팅하고, 요청 추적을 위한 Trace ID와 요청/응답 로그를 남기도록 설계했습니다.

## 프로젝트 목표

이 프로젝트는 단순히 서버를 실행하는 예제를 넘어서, 마이크로서비스 환경에서 Gateway가 맡는 역할을 직접 구현해 보는 데 목적이 있습니다.

- 클라이언트 진입점을 Gateway로 단일화
- 내부 서비스 라우팅 규칙 구성
- 요청 단위 Trace ID 생성 및 전파
- 요청/응답 로그 수집 기반 마련
- JWT 발급을 담당하는 인증 서버 분리
- Actuator와 Prometheus endpoint를 통한 운영 지표 노출

## 전체 구조

```text
gateway-server/
├── auth-server/       # JWT 발급 및 인증 관련 API 서버
└── gateway-server/    # Spring Cloud Gateway 기반 API Gateway 서버
```

## 서비스 구성

| 서비스 | 포트 | 역할 |
| --- | ---: | --- |
| auth-server | 8081 | 로그인 요청을 받아 JWT를 발급하고 테스트용 사용자 API를 제공합니다. |
| gateway-server | 8080 | 외부 요청을 받아 내부 서비스로 라우팅하고 Trace ID와 로그를 처리합니다. |

## 핵심 기능

### 1. API Gateway 라우팅

`gateway-server`는 `/api/users/**` 요청을 내부 인증 서버의 사용자 API로 전달하도록 설정되어 있습니다.

| 외부 요청 경로 | 내부 전달 대상 |
| --- | --- |
| `/api/users/**` | `auth-server` (`http://localhost:8081`) |

현재 라우팅 설정은 `gateway-server/src/main/resources/application.yaml`에서 관리합니다.  
`StripPrefix=1` 필터를 적용해 `/api/users/delay` 요청이 auth-server의 `/users/delay` endpoint로 전달됩니다.

### 2. Trace ID 생성 및 전파

Gateway는 요청에 `X-TraceId` 헤더가 없으면 UUID 기반 Trace ID를 생성합니다.  
이미 Trace ID가 있는 요청은 해당 값을 유지해 하위 서비스로 전달합니다.

이를 통해 여러 서비스에 걸친 하나의 요청 흐름을 로그에서 추적할 수 있습니다.

### 3. 요청/응답 로깅

Gateway 전역 필터에서 다음 정보를 로그로 남깁니다.

- Trace ID
- HTTP method
- 요청 path
- 응답 status
- 처리 시간
- 에러 메시지

이 구조는 장애 분석, 지연 요청 추적, API 호출 흐름 파악에 활용할 수 있습니다.

### 4. JWT 발급

`auth-server`는 `/auth/login` endpoint를 통해 JWT를 발급합니다.  
현재 구현은 학습 및 로컬 개발용으로 단순화되어 있으며, 전달받은 username을 subject로 사용해 토큰을 생성합니다.

## 기술 스택

- Java 25
- Spring Boot 4.0.6
- Spring Cloud Gateway WebFlux
- Spring Security
- JJWT
- Gradle Kotlin DSL
- Spring Boot Actuator
- Micrometer Prometheus
- Docker

## 실행 방법

각 서비스는 별도의 Gradle 프로젝트로 구성되어 있습니다.  
루트 디렉터리 기준으로 아래 순서대로 실행합니다.

### auth-server 실행

```bash
cd auth-server
./gradlew bootRun
```

Windows PowerShell에서는 다음 명령을 사용할 수 있습니다.

```powershell
cd auth-server
.\gradlew.bat bootRun
```

기본 포트는 `8081`입니다.

### gateway-server 실행

```bash
cd gateway-server
./gradlew bootRun
```

Windows PowerShell에서는 다음 명령을 사용할 수 있습니다.

```powershell
cd gateway-server
.\gradlew.bat bootRun
```

기본 포트는 `8080`입니다.

## API 예시

### JWT 발급

```bash
curl -X POST http://localhost:8081/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"test-user","password":"password"}'
```

응답 예시:

```json
{
  "token": "eyJ..."
}
```

### Gateway 상태 확인

```bash
curl http://localhost:8080/actuator/health
```

### Gateway 라우팅 확인

`/api/users/**` 라우팅을 실제로 확인하려면 `localhost:8081`에서 auth-server가 먼저 실행되어 있어야 합니다.

```bash
curl http://localhost:8080/api/users/delay
```

### Prometheus 지표 확인

```bash
curl http://localhost:8080/actuator/prometheus
```

## Docker 실행

`gateway-server`는 Dockerfile을 포함하고 있습니다.

```bash
cd gateway-server
docker build -t gateway-server .
docker run -p 8080:8080 gateway-server
```

## 주요 코드 위치

| 경로 | 설명 |
| --- | --- |
| `auth-server/src/main/java/.../controller/UserController.java` | 로그인 및 테스트 API endpoint |
| `auth-server/src/main/java/.../util/JwtUtil.java` | JWT 생성 로직 |
| `auth-server/src/main/java/.../config/SecurityConfig.java` | Spring Security 설정 |
| `gateway-server/src/main/resources/application.yaml` | Gateway 라우팅 및 Actuator 설정 |
| `gateway-server/src/main/java/.../config/TraceIdFilterConfig.java` | Trace ID 생성 및 헤더 전파 |
| `gateway-server/src/main/java/.../config/RequestLoggingFilterConfig.java` | 요청/응답 로그 필터 |

## 설계 포인트

- Gateway를 외부 진입점으로 두어 서비스 내부 구조를 클라이언트로부터 분리했습니다.
- Trace ID를 Gateway에서 먼저 보장해 downstream 서비스 로그와 연결할 수 있는 기반을 만들었습니다.
- 인증 서버와 Gateway 서버를 분리해 인증 책임과 라우팅 책임을 나누었습니다.
- Actuator endpoint를 열어 health check와 metric 수집이 가능하도록 했습니다.

## 향후 개선 방향

- Gateway에서 JWT 검증 필터 추가
- auth-server를 Gateway 뒤로 라우팅하도록 구조 정리
- user-service 등 별도 downstream 서비스 추가
- Resilience4j 기반 circuit breaker 및 fallback 적용
- Docker Compose로 전체 서비스 실행 환경 구성
- 운영 환경용 JWT secret 외부 주입 및 profile 분리
