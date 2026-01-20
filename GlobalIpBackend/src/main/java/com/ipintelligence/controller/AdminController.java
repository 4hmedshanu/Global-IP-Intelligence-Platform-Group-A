
package com.ipintelligence.controller;
import jakarta.annotation.PostConstruct;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Status;

import com.ipintelligence.dto.ApiHealthDTO;
import com.ipintelligence.metrics.EndpointMetricsService;
import com.ipintelligence.dto.SystemLogDTO;
import com.ipintelligence.dto.UserDto;
import com.ipintelligence.service.AdminDashboardService;
import com.ipintelligence.service.impl.AdminDashboardServiceImpl;
import com.ipintelligence.service.impl.DatabaseMetricsService;
import com.ipintelligence.service.impl.SystemLogService;


@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")



public class AdminController {
    // List of all monitored endpoints (shared for initialization)
    // Only endpoints actually used by the frontend
    private static final String[][] ENDPOINTS = new String[][] {
        {"/api/search/all", null},
        {"/api/search/patent/{externalId}", null},
        {"/api/tracker/subscribe", null},
        {"/api/tracker/unsubscribe", null},
        {"/api/tracker/filings/{assetId}", null},
        {"/api/tracker/analyst/lifecycle", null},
        {"/api/tracker/analyst/stats", null},
        {"/api/tracker/subscriptionsbyid", null},
        {"/api/dashboard/admin", null},
        {"/api/dashboard/user", null},
        {"/api/dashboard/analyst", null},
        {"/api/admin/users", null},
        {"/api/admin/db-metrics", null},
        {"/api/admin/dashboard/admin", null},
        {"/api/admin/api-health", null},
        {"/api/admin/logs", null},
        {"/api/profile", null},
        {"/api/search/asset/{assetId}", null},
        {"/api/search/history", null},
        {"/api/tracker/my", null},
        {"/api/login", null},
        {"/api/register", null}
    };


    // Initialize metrics for all endpoints at startup
    @PostConstruct
    public void initEndpointMetrics() {
        for (String[] ep : ENDPOINTS) {
            endpointMetricsService.getTimer(ep[0]);
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);
    @Autowired
    private EndpointMetricsService endpointMetricsService;

    @Autowired
    private HealthEndpoint healthEndpoint;

	@Autowired
     AdminDashboardServiceImpl adminService;
	
	
	@Autowired
	SystemLogService logService;
	
	@Autowired
	 DatabaseMetricsService dbMetricsService;

   
    // ✅ ADMIN CHECK API

    @GetMapping
    public ResponseEntity<String> getAdminData() {
        long start = System.nanoTime();
        ResponseEntity<String> response = ResponseEntity.ok("✅ ADMIN access granted! Full system control.");
        endpointMetricsService.getTimer("/api/admin").record(System.nanoTime() - start, java.util.concurrent.TimeUnit.NANOSECONDS);
        return response;
    }

    @GetMapping("/users")
    public List<UserDto> getAllUsers() {
        long start = System.nanoTime();
        List<UserDto> users = adminService.getAllUsers();
        endpointMetricsService.getTimer("/api/admin/users").record(System.nanoTime() - start, java.util.concurrent.TimeUnit.NANOSECONDS);
        return users;
    }

    // ✅ API HEALTH MONITORING
    private String actuatorBasePath;

    @GetMapping("/api-health")
    public List<ApiHealthDTO> getApiHealth() {
        Map<String, String> actuatorStatus = new HashMap<>();
        try {
            var health = healthEndpoint.health();
            Map<String, Object> components = null;
            try {
                var getComponents = health.getClass().getMethod("getComponents");
                var composite = (java.util.Map<?,?>) getComponents.invoke(health);
                if (composite != null) {
                    components = new java.util.HashMap<>();
                    for (var entry : composite.entrySet()) {
                        components.put(entry.getKey().toString(), entry.getValue());
                    }
                }
            } catch (NoSuchMethodException nsme) {
                // fallback for non-composite
                var getDetails = health.getClass().getMethod("getDetails");
                components = (Map<String, Object>) getDetails.invoke(health);
            }
            if (components != null) {
                for (String[] ep : ENDPOINTS) {
                    String indicator = ep[1];
                    if (indicator != null && components.containsKey(indicator)) {
                        Object comp = components.get(indicator);
                        String status = "UNKNOWN";
                        if (comp instanceof org.springframework.boot.actuate.health.Health) {
                            status = ((org.springframework.boot.actuate.health.Health) comp).getStatus().getCode();
                        } else if (comp instanceof Map) {
                            Object s = ((Map<?,?>)comp).get("status");
                            if (s != null) status = s.toString();
                        }
                        actuatorStatus.put(indicator, status);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error in getApiHealth endpoint", e);
        }

        List<ApiHealthDTO> result = new java.util.ArrayList<>();
        for (String[] ep : ENDPOINTS) {
            String endpoint = ep[0];
            String indicator = ep[1];
            String status = "UNKNOWN";
            if (indicator != null && actuatorStatus.containsKey(indicator)) {
                status = actuatorStatus.get(indicator);
            }
            // Get metrics
            long uptime = endpointMetricsService.getCount(endpoint);
            double avgResponse = endpointMetricsService.getAvgResponse(endpoint);
            String uptimeStr = uptime > 0 ? String.valueOf(uptime) : "-";
            String avgRespStr = avgResponse > 0 ? String.format("%.2f ms", avgResponse) : "-";
            result.add(new ApiHealthDTO(endpoint, status, uptimeStr, avgRespStr));
        }
        return result;
    }
    
    
    // ✅ ADD THIS
    @GetMapping("/db-metrics")
    public Map<String, Object> getDbMetrics() {
        long start = System.nanoTime();
        Map<String, Object> metrics = dbMetricsService.getMetrics();
        endpointMetricsService.getTimer("/api/admin/db-metrics").record(System.nanoTime() - start, java.util.concurrent.TimeUnit.NANOSECONDS);
        return metrics;
    }
    
    
    @GetMapping("/dashboard/admin")
    public Map<String, Object> getAdminDashboard() {
        long start = System.nanoTime();
        Map<String, Object> response = new HashMap<>();
        try {
            response.put("systemStats", List.of(
                Map.of(
                    "systemUsage", adminService.getSystemUsageTrends()
                )
            ));
            System.out.print(adminService.getSystemUsageTrends());
        } catch (Exception e) {
            // Optionally log the error, e.g. logger.error("Error in getAdminDashboard", e);
        }
        endpointMetricsService.getTimer("/api/admin/dashboard/admin").record(System.nanoTime() - start, java.util.concurrent.TimeUnit.NANOSECONDS);
        return response;
    }
    
    
}
