package com.project.userservice.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Set;

/**
 * API Version Interceptor
 * 
 * Intercepts API requests to validate version compatibility and add version headers.
 * Supports multiple versioning strategies:
 * 1. URL Path versioning (primary): /api/v1/customers
 * 2. Header versioning (fallback): API-Version: v1
 */
@Component
@Slf4j
public class ApiVersionInterceptor implements HandlerInterceptor {
    
    private static final Set<String> SUPPORTED_VERSIONS = Set.of("v1");
    private static final String DEPRECATED_VERSION_WARNING = "API-Deprecated";
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String requestURI = request.getRequestURI();
        
        // Extract version from URL path
        String version = extractVersionFromPath(requestURI);
        
        if (version != null) {
            // Validate version
            if (!SUPPORTED_VERSIONS.contains(version)) {
                log.warn("Unsupported API version requested: {} from {}", version, request.getRemoteAddr());
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.setHeader("Content-Type", "application/json");
                try {
                    response.getWriter().write(
                        "{\"error\": \"Unsupported API version: " + version + 
                        "\", \"supportedVersions\": [" + String.join(",", SUPPORTED_VERSIONS) + "]}"
                    );
                } catch (Exception e) {
                    log.error("Error writing version error response", e);
                }
                return false;
            }
            
            // Add version to response headers
            response.setHeader(ApiVersionConfig.API_VERSION_HEADER, version);
            
            // Add deprecation warning if needed (example for future versions)
            // if ("v1".equals(version)) {
            //     response.setHeader(DEPRECATED_VERSION_WARNING, "This version will be deprecated on 2024-12-31");
            // }
            
            log.debug("API request for version: {} to endpoint: {}", version, requestURI);
        }
        
        return true;
    }
    
    private String extractVersionFromPath(String path) {
        if (path.startsWith("/api/v")) {
            String[] pathParts = path.split("/");
            if (pathParts.length >= 3 && pathParts[2].startsWith("v")) {
                return pathParts[2]; // Returns "v1", "v2", etc.
            }
        }
        return null;
    }
}