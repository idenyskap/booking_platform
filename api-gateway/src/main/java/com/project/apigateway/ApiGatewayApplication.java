package com.project.apigateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JAutoConfiguration;
import org.springframework.context.annotation.Import;

/**
 * Spring Cloud Gateway Application
 * 
 * Features:
 * - JWT Authentication with Keycloak
 * - Service Discovery with Eureka
 * - Rate Limiting with Redis
 * - Circuit Breaker with Resilience4j
 * - Request/Response Logging
 * - CORS Configuration
 * - Health Checks and Monitoring
 */
@SpringBootApplication
@EnableDiscoveryClient
@Import(ReactiveResilience4JAutoConfiguration.class)
public class ApiGatewayApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}