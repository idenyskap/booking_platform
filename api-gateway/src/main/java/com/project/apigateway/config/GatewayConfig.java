package com.project.apigateway.config;

import com.project.apigateway.filter.InMemoryRateLimitingFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

/**
 * Gateway routing configuration
 * Defines routes to all microservices with filters for authentication, rate limiting, and circuit breaking
 */
@Configuration
@RequiredArgsConstructor
public class GatewayConfig {

    private final InMemoryRateLimitingFilter rateLimitingFilter;

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
            // User Service Routes
            .route("user-service-customers", r -> r
                .path("/api/v*/customers/**")
                .and()
                .method(HttpMethod.GET, HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE)
                .filters(f -> f
                    .stripPrefix(2) // Remove /api/v1 from path
                    .circuitBreaker(config -> config
                        .setName("user-service-cb")
                        .setFallbackUri("forward:/fallback/user-service"))
                    .filter(rateLimitingFilter.apply(createRateLimitConfig(50, 60)))
                    .retry(retryConfig -> retryConfig.setRetries(3))
                )
                .uri("lb://user-service"))
            
            .route("user-service-providers", r -> r
                .path("/api/v*/providers/**")
                .filters(f -> f
                    .stripPrefix(2)
                    .circuitBreaker(config -> config
                        .setName("user-service-cb")
                        .setFallbackUri("forward:/fallback/user-service"))
                    .filter(rateLimitingFilter.apply(createRateLimitConfig(100, 60)))
                    .retry(retryConfig -> retryConfig.setRetries(3))
                )
                .uri("lb://user-service"))
            
            .route("user-service-business-owners", r -> r
                .path("/api/v*/business-owners/**")
                .filters(f -> f
                    .stripPrefix(2)
                    .circuitBreaker(config -> config
                        .setName("user-service-cb")
                        .setFallbackUri("forward:/fallback/user-service"))
                    .filter(rateLimitingFilter.apply(createRateLimitConfig(100, 60)))
                    .retry(retryConfig -> retryConfig.setRetries(3))
                )
                .uri("lb://user-service"))
            
            .route("user-service-users", r -> r
                .path("/api/v*/users/**")
                .filters(f -> f
                    .stripPrefix(2)
                    .circuitBreaker(config -> config
                        .setName("user-service-cb")
                        .setFallbackUri("forward:/fallback/user-service"))
                    .filter(rateLimitingFilter.apply(createRateLimitConfig(30, 60)))
                    .retry(retryConfig -> retryConfig.setRetries(3))
                )
                .uri("lb://user-service"))

            // Booking Service Routes
            .route("booking-service", r -> r
                .path("/api/v*/bookings/**")
                .filters(f -> f
                    .stripPrefix(2)
                    .circuitBreaker(config -> config
                        .setName("booking-service-cb")
                        .setFallbackUri("forward:/fallback/booking-service"))
                    .filter(rateLimitingFilter.apply(createRateLimitConfig(200, 60)))
                    .retry(retryConfig -> retryConfig.setRetries(3))
                )
                .uri("lb://booking-service"))

            // Business Service Routes
            .route("business-service", r -> r
                .path("/api/v*/businesses/**")
                .filters(f -> f
                    .stripPrefix(2)
                    .circuitBreaker(config -> config
                        .setName("business-service-cb")
                        .setFallbackUri("forward:/fallback/business-service"))
                    .filter(rateLimitingFilter.apply(createRateLimitConfig(150, 60)))
                    .retry(retryConfig -> retryConfig.setRetries(3))
                )
                .uri("lb://business-service"))

            // Payment Service Routes
            .route("payment-service", r -> r
                .path("/api/v*/payments/**")
                .filters(f -> f
                    .stripPrefix(2)
                    .circuitBreaker(config -> config
                        .setName("payment-service-cb")
                        .setFallbackUri("forward:/fallback/payment-service"))
                    .filter(rateLimitingFilter.apply(createRateLimitConfig(100, 60)))
                    .retry(retryConfig -> retryConfig.setRetries(3))
                )
                .uri("lb://payment-service"))

            // Authentication Routes (Direct to Keycloak - no auth required)
            .route("auth-service", r -> r
                .path("/api/auth/**")
                .filters(f -> f
                    .stripPrefix(2)
                    .filter(rateLimitingFilter.apply(createRateLimitConfig(20, 60)))
                    .addRequestHeader("X-Forwarded-Host", "localhost:8080")
                )
                .uri("http://localhost:8090"))

            // Health check routes
            .route("health-checks", r -> r
                .path("/health/**")
                .filters(f -> f
                    .stripPrefix(1)
                    .addResponseHeader("X-Health-Check", "true")
                )
                .uri("lb://user-service"))

            .build();
    }

    private InMemoryRateLimitingFilter.Config createRateLimitConfig(int requestsPerWindow, int windowSizeInSeconds) {
        InMemoryRateLimitingFilter.Config config = new InMemoryRateLimitingFilter.Config();
        config.setRequestsPerWindow(requestsPerWindow);
        config.setWindowSizeInSeconds(windowSizeInSeconds);
        return config;
    }
}