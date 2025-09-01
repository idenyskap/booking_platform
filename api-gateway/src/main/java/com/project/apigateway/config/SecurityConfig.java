package com.project.apigateway.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import reactor.core.publisher.Flux;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .authorizeExchange(exchanges -> exchanges
                // Public endpoints
                .pathMatchers("/actuator/health", "/actuator/info").permitAll()
                .pathMatchers("/api/auth/**").permitAll()
                
                // Registration endpoints - require authentication but not specific roles
                .pathMatchers(HttpMethod.POST, "/api/v*/customers/register").authenticated()
                .pathMatchers(HttpMethod.POST, "/api/v*/providers/register").authenticated()
                .pathMatchers(HttpMethod.POST, "/api/v*/business-owners/register").authenticated()
                
                // Public read endpoints for providers and business owners
                .pathMatchers(HttpMethod.GET, "/api/v*/providers/**").permitAll()
                .pathMatchers(HttpMethod.GET, "/api/v*/business-owners/business/**").permitAll()
                .pathMatchers(HttpMethod.GET, "/api/v*/business-owners/search").permitAll()
                .pathMatchers(HttpMethod.GET, "/api/v*/business-owners/by-type/**").permitAll()
                
                // Admin endpoints
                .pathMatchers("/api/v*/admin/**").hasRole("ADMIN")
                .pathMatchers(HttpMethod.DELETE, "/api/v*/**").hasRole("ADMIN")
                .pathMatchers("/api/v*/*/verify").hasRole("ADMIN")
                .pathMatchers("/api/v*/*/suspend").hasRole("ADMIN")
                
                // Customer endpoints
                .pathMatchers("/api/v*/customers/profile").hasAnyRole("CUSTOMER", "ADMIN")
                .pathMatchers(HttpMethod.PUT, "/api/v*/customers/profile").hasRole("CUSTOMER")
                
                // Provider endpoints
                .pathMatchers("/api/v*/providers/profile").hasAnyRole("PROVIDER", "ADMIN")
                .pathMatchers(HttpMethod.PUT, "/api/v*/providers/profile").hasRole("PROVIDER")
                
                // Business owner endpoints
                .pathMatchers("/api/v*/business-owners/profile").hasAnyRole("BUSINESS_OWNER", "ADMIN")
                .pathMatchers(HttpMethod.PUT, "/api/v*/business-owners/profile").hasRole("BUSINESS_OWNER")
                
                // All other endpoints require authentication
                .anyExchange().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .jwtAuthenticationConverter(reactiveJwtAuthenticationConverter())
                )
            )
            .build();
    }

    @Bean
    public ReactiveJwtAuthenticationConverter reactiveJwtAuthenticationConverter() {
        ReactiveJwtAuthenticationConverter converter = new ReactiveJwtAuthenticationConverter();
        
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            // Extract roles from realm_access.roles
            Map<String, Object> realmAccess = jwt.getClaim("realm_access");
            Collection<String> roles = List.of();
            
            if (realmAccess != null && realmAccess.get("roles") instanceof List) {
                roles = (List<String>) realmAccess.get("roles");
            }
            
            // Convert roles to granted authorities with ROLE_ prefix
            return Flux.fromIterable(roles)
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()));
        });
        
        converter.setPrincipalClaimName("sub"); // Use subject as principal name
        return converter;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setExposedHeaders(List.of("Authorization", "X-Total-Count", "X-Request-ID"));
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}