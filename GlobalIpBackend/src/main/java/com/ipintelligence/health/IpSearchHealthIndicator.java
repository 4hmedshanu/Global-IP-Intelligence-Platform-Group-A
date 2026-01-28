package com.ipintelligence.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import com.ipintelligence.service.IpSearchService;

@Component
public class IpSearchHealthIndicator implements HealthIndicator {

    @Autowired
    private IpSearchService ipSearchService;

    @Override
    public Health health() {
        try {
            // Implement a lightweight check, e.g., a ping or test query
            boolean available = ipSearchService.isAvailable();
            if (available) {
                return Health.up().withDetail("/ip/search", "UP").build();
            } else {
                return Health.down().withDetail("/ip/search", "DOWN").build();
            }
        } catch (Exception e) {
            return Health.down(e).withDetail("/ip/search", "ERROR").build();
        }
    }
}
