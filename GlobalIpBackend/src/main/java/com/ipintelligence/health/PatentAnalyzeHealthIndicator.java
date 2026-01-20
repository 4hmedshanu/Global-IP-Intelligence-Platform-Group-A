package com.ipintelligence.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import com.ipintelligence.service.GooglePatentService;

@Component
public class PatentAnalyzeHealthIndicator implements HealthIndicator {

    @Autowired
    private GooglePatentService googlePatentService;

    @Override
    public Health health() {
        try {
            boolean available = googlePatentService.isAvailable();
            if (available) {
                return Health.up().withDetail("/patent/analyze", "UP").build();
            } else {
                return Health.down().withDetail("/patent/analyze", "DOWN").build();
            }
        } catch (Exception e) {
            return Health.down(e).withDetail("/patent/analyze", "ERROR").build();
        }
    }
}
