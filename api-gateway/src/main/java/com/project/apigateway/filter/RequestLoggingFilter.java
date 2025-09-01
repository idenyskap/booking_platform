package com.project.apigateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

/**
 * Global filter for request logging and tracing
 * Adds request ID and logs request/response information
 */
@Component
@Slf4j
public class RequestLoggingFilter implements GlobalFilter, Ordered {

    private static final String REQUEST_ID_HEADER = "X-Request-ID";
    private static final String REQUEST_START_TIME = "request_start_time";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        
        // Generate request ID if not present
        String requestId = request.getHeaders().getFirst(REQUEST_ID_HEADER);
        if (requestId == null) {
            requestId = UUID.randomUUID().toString();
        }
        
        // Add request ID to request and response headers
        ServerHttpRequest modifiedRequest = request.mutate()
            .header(REQUEST_ID_HEADER, requestId)
            .build();
        
        exchange.getResponse().getHeaders().add(REQUEST_ID_HEADER, requestId);
        
        // Store start time for request duration calculation
        exchange.getAttributes().put(REQUEST_START_TIME, Instant.now());
        
        // Log incoming request
        logRequest(modifiedRequest, requestId);
        
        final String finalRequestId = requestId;
        return chain.filter(exchange.mutate().request(modifiedRequest).build())
            .doFinally(signalType -> {
                // Log response
                logResponse(exchange, finalRequestId);
            });
    }

    private void logRequest(ServerHttpRequest request, String requestId) {
        String method = request.getMethod().name();
        String path = request.getURI().getPath();
        String query = request.getURI().getQuery();
        String userAgent = request.getHeaders().getFirst("User-Agent");
        String clientIp = getClientIp(request);
        
        log.info("REQUEST [{}] {} {} {} - Client: {} - UA: {}", 
                requestId, method, path, 
                query != null ? "?" + query : "",
                clientIp, userAgent);
    }

    private void logResponse(ServerWebExchange exchange, String requestId) {
        Instant startTime = (Instant) exchange.getAttributes().get(REQUEST_START_TIME);
        long duration = startTime != null ? 
            Instant.now().toEpochMilli() - startTime.toEpochMilli() : 0;
        
        int statusCode = exchange.getResponse().getStatusCode() != null ? 
            exchange.getResponse().getStatusCode().value() : 0;
        
        log.info("RESPONSE [{}] {} - Duration: {}ms", 
                requestId, statusCode, duration);
    }

    private String getClientIp(ServerHttpRequest request) {
        String xForwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeaders().getFirst("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddress() != null ? 
            request.getRemoteAddress().getAddress().getHostAddress() : "unknown";
    }

    @Override
    public int getOrder() {
        return -200; // Run before authentication filter
    }
}