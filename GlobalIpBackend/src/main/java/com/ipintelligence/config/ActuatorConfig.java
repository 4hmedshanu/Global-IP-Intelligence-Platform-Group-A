package com.ipintelligence.config;


import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.health.HealthContributorRegistry;
import org.springframework.boot.actuate.health.HealthEndpointGroups;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.time.Duration;

@Configuration
public class ActuatorConfig {
    @Bean
    public HealthEndpoint healthEndpoint(HealthContributorRegistry registry,
                                         HealthEndpointGroups groups) {
        // Use a default timeout, e.g., 5 seconds
        return new HealthEndpoint(registry, groups, Duration.ofSeconds(5));
    }
}