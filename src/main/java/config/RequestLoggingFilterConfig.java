package config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.reactive.ServerHttpRequest;

@Configuration
public class RequestLoggingFilterConfig {

    private static final Logger logger = LoggerFactory.getLogger(RequestLoggingFilterConfig.class);
    private static final String Trace_ID_HEADER = "X-TraceId";

    @Order(Ordered.LOWEST_PRECEDENCE)
    @Bean
    public GlobalFilter requestLoggingFilter()
    {
        return ((exchange, chain) ->  {

            long startTime = System.currentTimeMillis();

            var request = exchange.getRequest();

            // traceId, method, path를 logging
            String traceId = request.getHeaders().getFirst(Trace_ID_HEADER); // getFirst
            String method = request.getMethod().name(); // 이게 null 일 수가 없지 null이면 http 요청이 못오지않나 ?
            String path = request.getURI().getPath();

            logger.info("[REQUEST] traceId = {}, method = {}, path = {}", traceId, method, path);

            return chain.filter(exchange)
                    .doFinally(signalType ->
                            {
                            long endTime = System.currentTimeMillis() -  startTime;

                            var response = exchange.getResponse();
                                Integer statusCode = response.getStatusCode() != null
                                        ? response.getStatusCode().value()
                                        : null;

                                logger.info("[RESPONSE] traceId={}, method={}, path={}, status={}, durationMs={}, signal={}",
                                        traceId, method, path, statusCode, endTime, signalType);
                            });

        });
    }
}
