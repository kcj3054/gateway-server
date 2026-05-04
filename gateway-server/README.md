# gateway-server

Spring Cloud Gateway 기반 API Gateway 서버

## 기능

- 요청/응답 로깅 (traceId, method, path, status, duration)
- TraceId 자동 생성 및 전파 (X-TraceId 헤더)
- Actuator 엔드포인트 (health, metrics, prometheus)

## 라우팅

| 경로 | 대상 서비스 |
|------|------------|
| `/api/users/**` | user-service (localhost:8081) |

---

## 로컬 실행

```bash
./gradlew bootRun
```

기본 포트: `8080`

---

## Docker 빌드 & 실행

### 빌드

```bash
docker build -t gateway-server .
```

### 실행

```bash
docker run -p 8080:8080 gateway-server
```

### 환경변수 주입 (선택)

```bash
docker run -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  gateway-server
```

---

## 헬스체크

```bash
curl http://localhost:8080/actuator/health
```
