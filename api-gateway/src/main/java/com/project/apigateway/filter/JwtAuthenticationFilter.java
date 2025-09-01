package com.project.apigateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

/**
 * Global filter for JWT authentication and token validation
 * Runs for all requests and adds authentication context to downstream services
 */
@Component
@Slf4j
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String USER_ID_HEADER = "X-User-ID";
    private static final String USER_ROLES_HEADER = "X-User-Roles";
    private static final String USER_EMAIL_HEADER = "X-User-Email";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        
        // Skip authentication for public endpoints
        if (isPublicEndpoint(path)) {
            log.debug("Skipping authentication for public endpoint: {}", path);
            return chain.filter(exchange);
        }

        return ReactiveSecurityContextHolder.getContext()
            .cast(org.springframework.security.core.context.SecurityContext.class)
            .map(securityContext -> securityContext.getAuthentication())
            .cast(Authentication.class)
            .flatMap(authentication -> {
                if (authentication != null && authentication.isAuthenticated()) {
                    return processAuthenticatedRequest(exchange, chain, authentication);
                } else {
                    return handleUnauthenticatedRequest(exchange);
                }
            })
            .switchIfEmpty(handleUnauthenticatedRequest(exchange));
    }

    private Mono<Void> processAuthenticatedRequest(ServerWebExchange exchange, 
                                                  GatewayFilterChain chain, 
                                                  Authentication authentication) {
        try {
            Jwt jwt = (Jwt) authentication.getPrincipal();
            
            // Extract user information from JWT
            String userId = jwt.getSubject();
            String email = jwt.getClaimAsString("email");
            String preferredUsername = jwt.getClaimAsString("preferred_username");
            
            // Extract roles
            List<String> roles = extractRoles(jwt);
            
            // Add headers for downstream services
            ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                .header(USER_ID_HEADER, userId)
                .header(USER_EMAIL_HEADER, email != null ? email : "")
                .header(USER_ROLES_HEADER, String.join(",", roles))
                .build();
            
            log.debug("Authentication successful for user: {} with roles: {}", 
                     preferredUsername != null ? preferredUsername : userId, roles);
            
            return chain.filter(exchange.mutate().request(modifiedRequest).build());
            
        } catch (Exception e) {
            log.error("Error processing authenticated request", e);
            return handleAuthenticationError(exchange);
        }
    }

    private Mono<Void> handleUnauthenticatedRequest(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        String path = exchange.getRequest().getURI().getPath();
        
        log.warn("Unauthenticated request to protected endpoint: {}", path);
        
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, "application/json");
        
        String body = "{\"error\": \"Unauthorized\", \"message\": \"Valid authentication token required\"}";
        return response.writeWith(Mono.just(response.bufferFactory().wrap(body.getBytes())));
    }

    private Mono<Void> handleAuthenticationError(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, "application/json");
        
        String body = "{\"error\": \"Authentication Error\", \"message\": \"Invalid or expired token\"}";
        return response.writeWith(Mono.just(response.bufferFactory().wrap(body.getBytes())));
    }

    @SuppressWarnings("unchecked")
    private List<String> extractRoles(Jwt jwt) {
        try {
            Map<String, Object> realmAccess = jwt.getClaim("realm_access");
            if (realmAccess != null && realmAccess.get("roles") instanceof List) {
                return (List<String>) realmAccess.get("roles");
            }
        } catch (Exception e) {
            log.warn("Error extracting roles from JWT", e);
        }
        return List.of();
    }

    private boolean isPublicEndpoint(String path) {
        return path.matches(".*/actuator/health") ||
               path.matches(".*/actuator/info") ||
               path.matches(".*/api/auth/.*") ||
               path.matches(".*/api/v.*/providers") ||
               path.matches(".*/api/v.*/providers/.*") ||
               path.matches(".*/api/v.*/business-owners/business/.*") ||
               path.matches(".*/api/v.*/business-owners/search") ||
               path.matches(".*/api/v.*/business-owners/by-type/.*");
    }

    @Override
    public int getOrder() {
        return -100; // Run early in the filter chain
    }
}