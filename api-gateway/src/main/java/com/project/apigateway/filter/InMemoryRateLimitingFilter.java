package com.project.apigateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * In-memory rate limiting filter
 * Simple rate limiting without Redis dependency
 */
@Component
@Slf4j
public class InMemoryRateLimitingFilter extends AbstractGatewayFilterFactory<InMemoryRateLimitingFilter.Config> {

    private final ConcurrentHashMap<String, RateLimitBucket> rateLimitBuckets = new ConcurrentHashMap<>();

    public InMemoryRateLimitingFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String key = generateKey(exchange, config);
            
            if (checkRateLimit(key, config)) {
                return chain.filter(exchange);
            } else {
                return handleRateLimitExceeded(exchange, config);
            }
        };
    }

    private String generateKey(org.springframework.web.server.ServerWebExchange exchange, Config config) {
        String clientIp = getClientIp(exchange);
        String path = exchange.getRequest().getURI().getPath();
        return String.format("rate_limit:%s:%s:%s", config.getKeyResolver(), clientIp, path);
    }

    private String getClientIp(org.springframework.web.server.ServerWebExchange exchange) {
        String xForwardedFor = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return exchange.getRequest().getRemoteAddress() != null ? 
            exchange.getRequest().getRemoteAddress().getAddress().getHostAddress() : "unknown";
    }

    private boolean checkRateLimit(String key, Config config) {
        long currentTime = Instant.now().getEpochSecond();
        long windowStart = currentTime / config.getWindowSizeInSeconds() * config.getWindowSizeInSeconds();
        
        RateLimitBucket bucket = rateLimitBuckets.computeIfAbsent(key, k -> new RateLimitBucket());
        
        synchronized (bucket) {
            // Reset bucket if we're in a new window
            if (bucket.windowStart != windowStart) {
                bucket.windowStart = windowStart;
                bucket.requestCount.set(0);
            }
            
            int currentCount = bucket.requestCount.incrementAndGet();
            boolean allowed = currentCount <= config.getRequestsPerWindow();
            
            if (!allowed) {
                log.warn("Rate limit exceeded for key: {} (count: {}, limit: {})", 
                        key, currentCount, config.getRequestsPerWindow());
            }
            
            return allowed;
        }
    }

    private Mono<Void> handleRateLimitExceeded(org.springframework.web.server.ServerWebExchange exchange, Config config) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        response.getHeaders().add("Content-Type", "application/json");
        response.getHeaders().add("X-RateLimit-Remaining", "0");
        response.getHeaders().add("X-RateLimit-Retry-After-Seconds", String.valueOf(config.getWindowSizeInSeconds()));
        
        String body = String.format(
            "{\"error\": \"Rate limit exceeded\", \"message\": \"Maximum %d requests per %d seconds allowed\"}",
            config.getRequestsPerWindow(), config.getWindowSizeInSeconds()
        );
        
        return response.writeWith(Mono.just(response.bufferFactory().wrap(body.getBytes())));
    }

    // Clean up old buckets periodically (simple cleanup)
    public void cleanup() {
        long currentTime = Instant.now().getEpochSecond();
        rateLimitBuckets.entrySet().removeIf(entry -> {
            RateLimitBucket bucket = entry.getValue();
            return (currentTime - bucket.windowStart) > 3600; // Remove buckets older than 1 hour
        });
    }

    private static class RateLimitBucket {
        volatile long windowStart = 0;
        final AtomicInteger requestCount = new AtomicInteger(0);
    }

    public static class Config {
        private int requestsPerWindow = 100;
        private int windowSizeInSeconds = 60;
        private String keyResolver = "default";

        // Getters and setters
        public int getRequestsPerWindow() { return requestsPerWindow; }
        public void setRequestsPerWindow(int requestsPerWindow) { this.requestsPerWindow = requestsPerWindow; }
        
        public int getWindowSizeInSeconds() { return windowSizeInSeconds; }
        public void setWindowSizeInSeconds(int windowSizeInSeconds) { this.windowSizeInSeconds = windowSizeInSeconds; }
        
        public String getKeyResolver() { return keyResolver; }
        public void setKeyResolver(String keyResolver) { this.keyResolver = keyResolver; }
    }
}