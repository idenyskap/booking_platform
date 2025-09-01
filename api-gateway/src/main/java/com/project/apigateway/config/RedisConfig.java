package com.project.apigateway.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

/**
 * Redis configuration for rate limiting
 * This class will be populated when Redis dependency is enabled
 * 
 * To enable Redis:
 * 1. Uncomment Redis dependency in pom.xml
 * 2. Uncomment Redis configuration in application.yml
 * 3. Add the Redis template bean configuration
 */
@Configuration
@Slf4j
public class RedisConfig {
    
    // Redis configuration will be added here when Redis dependency is enabled
    // @Bean
    // @ConditionalOnProperty(name = "spring.data.redis.host")
    // public ReactiveRedisTemplate<String, String> reactiveRedisTemplate(
    //         ReactiveRedisConnectionFactory connectionFactory) {
    //     // Redis template configuration
    // }
}