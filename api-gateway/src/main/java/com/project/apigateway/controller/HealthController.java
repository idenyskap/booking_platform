package com.project.apigateway.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Health check controller for API Gateway
 * Provides detailed health information about gateway and downstream services
 */
@RestController
@RequestMapping("/health")
@Slf4j
public class HealthController {

    @Autowired
    private DiscoveryClient discoveryClient;

    @Autowired
    private RouteLocator routeLocator;

    @GetMapping("/gateway")
    public Mono<ResponseEntity<Map<String, Object>>> getGatewayHealth() {
        Map<String, Object> health = new HashMap<>();
        
        try {
            // Gateway status
            health.put("status", "UP");
            health.put("timestamp", LocalDateTime.now());
            health.put("gateway", "api-gateway");
            
            // Discovery client info
            health.put("discoveredServices", discoveryClient.getServices());
            
            // Routes info
            health.put("activeRoutes", routeLocator.getRoutes()
                .map(route -> Map.of(
                    "id", route.getId(),
                    "uri", route.getUri().toString(),
                    "filters", route.getFilters().toString()
                ))
                .collectList()
                .block());
            
            // Service instances
            Map<String, Object> serviceInstances = new HashMap<>();
            for (String service : discoveryClient.getServices()) {
                serviceInstances.put(service, discoveryClient.getInstances(service).size());
            }
            health.put("serviceInstances", serviceInstances);
            
            return Mono.just(ResponseEntity.ok(health));
            
        } catch (Exception e) {
            log.error("Error getting gateway health", e);
            health.put("status", "DOWN");
            health.put("error", e.getMessage());
            return Mono.just(ResponseEntity.status(503).body(health));
        }
    }

    @GetMapping("/services")
    public Mono<ResponseEntity<Map<String, Object>>> getServicesHealth() {
        Map<String, Object> servicesHealth = new HashMap<>();
        
        try {
            for (String serviceName : discoveryClient.getServices()) {
                Map<String, Object> serviceInfo = new HashMap<>();
                var instances = discoveryClient.getInstances(serviceName);
                
                serviceInfo.put("instanceCount", instances.size());
                serviceInfo.put("instances", instances.stream()
                    .map(instance -> Map.of(
                        "instanceId", instance.getInstanceId(),
                        "host", instance.getHost(),
                        "port", instance.getPort(),
                        "uri", instance.getUri().toString(),
                        "metadata", instance.getMetadata()
                    ))
                    .collect(Collectors.toList()));
                
                servicesHealth.put(serviceName, serviceInfo);
            }
            
            return Mono.just(ResponseEntity.ok(Map.of(
                "status", "UP",
                "timestamp", LocalDateTime.now(),
                "services", servicesHealth
            )));
            
        } catch (Exception e) {
            log.error("Error getting services health", e);
            return Mono.just(ResponseEntity.status(503).body(Map.of(
                "status", "DOWN",
                "error", e.getMessage()
            )));
        }
    }
}