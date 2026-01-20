package com.ipintelligence.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class EndpointMetricsService {

    private final MeterRegistry meterRegistry;
    private final Map<String, Timer> timers = new ConcurrentHashMap<>();

    public EndpointMetricsService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public Timer getTimer(String endpoint) {
        return timers.computeIfAbsent(endpoint, ep -> Timer.builder("api.endpoint.response")
                .tag("endpoint", ep)
                .register(meterRegistry));
    }

    public double getAvgResponse(String endpoint) {
        Timer timer = timers.get(endpoint);
        if (timer == null || timer.count() == 0) {
            return -1;
        }
        return timer.mean(java.util.concurrent.TimeUnit.MILLISECONDS);
    }

    public long getCount(String endpoint) {
        Timer timer = timers.get(endpoint);
        return timer == null ? 0 : timer.count();
    }

    public double getTotalTime(String endpoint) {
        Timer timer = timers.get(endpoint);
        return timer == null ? 0 : timer.totalTime(java.util.concurrent.TimeUnit.MILLISECONDS);
    }
}
