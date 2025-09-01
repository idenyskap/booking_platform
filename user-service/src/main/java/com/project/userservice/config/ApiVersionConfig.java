package com.project.userservice.config;

import org.springframework.context.annotation.Configuration;

/**
 * API Versioning Configuration
 * 
 * Current versioning strategy: URL Path versioning
 * Format: /api/v{version}/{resource}
 * 
 * Supported versions:
 * - v1: Initial API version with basic CRUD operations
 * - v2: Enhanced API with additional features (future)
 * 
 * Version lifecycle:
 * - v1: Current stable version
 * - v2: Next major version (breaking changes allowed)
 * 
 * Deprecation policy:
 * - Versions supported for minimum 12 months after new version release
 * - Breaking changes only in major versions (v1 -> v2)
 * - Minor updates within same version for bug fixes and compatible features
 */
@Configuration
public class ApiVersionConfig {
    
    public static final String CURRENT_VERSION = "v1";
    public static final String API_VERSION_HEADER = "API-Version";
    
    // Version constants
    public static final String V1 = "/api/v1";
    public static final String V2 = "/api/v2"; // Future version
    
    // Resource paths
    public static final String CUSTOMERS = "/customers";
    public static final String PROVIDERS = "/providers";
    public static final String BUSINESS_OWNERS = "/business-owners";
    public static final String USERS = "/users";
}