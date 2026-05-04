package config;

import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Configuration
public class TraceIdFilterConfig {

    private static final  String TRACE_ID_HEADER = "X-TraceId";


    @Bean
    public GlobalFilter traceIdFilter() {

        return (exchange, chain) -> {

            final String traceIdToUse = resolveTraceId(exchange); // effectively final로도 가능 함

            var mutateRequest = exchange.getRequest().mutate().header(TRACE_ID_HEADER, traceIdToUse).build();

            var mutateExchange = exchange.mutate().request(mutateRequest).build();

            return chain.filter(mutateExchange).
                    then(Mono.fromRunnable(() -> {
                        System.out.println(traceIdToUse + "path=" +  exchange.getRequest().getPath());
                    }));

        };

    }

    private String resolveTraceId(ServerWebExchange exchange) {

        String  traceId = exchange.getRequest().getHeaders().getFirst(TRACE_ID_HEADER);

        if(traceId == null || traceId.isEmpty()) {
            traceId = UUID.randomUUID().toString();
        }
        return traceId;
    }

}
