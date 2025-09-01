package com.project.apigateway.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Fallback controller for circuit breaker patterns
 * Provides fallback responses when downstream services are unavailable
 */
@RestController
@RequestMapping("/fallback")
@Slf4j
public class FallbackController {

    @GetMapping("/user-service")
    @PostMapping("/user-service")
    public Mono<ResponseEntity<Map<String, Object>>> userServiceFallback() {
        log.warn("User service fallback triggered");
        
        Map<String, Object> fallbackResponse = Map.of(
            "error", "Service Temporarily Unavailable",
            "message", "User service is currently experiencing issues. Please try again later.",
            "service", "user-service",
            "timestamp", LocalDateTime.now(),
            "fallback", true
        );
        
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(fallbackResponse));
    }

    @GetMapping("/booking-service")
    @PostMapping("/booking-service")
    public Mono<ResponseEntity<Map<String, Object>>> bookingServiceFallback() {
        log.warn("Booking service fallback triggered");
        
        Map<String, Object> fallbackResponse = Map.of(
            "error", "Service Temporarily Unavailable",
            "message", "Booking service is currently experiencing issues. Please try again later.",
            "service", "booking-service",
            "timestamp", LocalDateTime.now(),
            "fallback", true
        );
        
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(fallbackResponse));
    }

    @GetMapping("/business-service")
    @PostMapping("/business-service")
    public Mono<ResponseEntity<Map<String, Object>>> businessServiceFallback() {
        log.warn("Business service fallback triggered");
        
        Map<String, Object> fallbackResponse = Map.of(
            "error", "Service Temporarily Unavailable",
            "message", "Business service is currently experiencing issues. Please try again later.",
            "service", "business-service",
            "timestamp", LocalDateTime.now(),
            "fallback", true
        );
        
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(fallbackResponse));
    }

    @GetMapping("/payment-service")
    @PostMapping("/payment-service")
    public Mono<ResponseEntity<Map<String, Object>>> paymentServiceFallback() {
        log.warn("Payment service fallback triggered");
        
        Map<String, Object> fallbackResponse = Map.of(
            "error", "Service Temporarily Unavailable",
            "message", "Payment service is currently experiencing issues. Please try again later.",
            "service", "payment-service",
            "timestamp", LocalDateTime.now(),
            "fallback", true,
            "retryAfter", "Please retry your payment in a few minutes"
        );
        
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(fallbackResponse));
    }
}