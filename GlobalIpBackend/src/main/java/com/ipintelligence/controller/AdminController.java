package com.ipintelligence.controller;

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

import com.ipintelligence.dto.ApiHealthDTO;
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

	@Autowired
     AdminDashboardServiceImpl adminService;
	
	
	@Autowired
	SystemLogService logService;
	
	@Autowired
	 DatabaseMetricsService dbMetricsService;

   
    // ✅ ADMIN CHECK API
    @GetMapping
    public ResponseEntity<String> getAdminData() {
        return ResponseEntity.ok("✅ ADMIN access granted! Full system control.");
    }

    // ✅ USER MANAGEMENT API
    @GetMapping("/users")
    public List<UserDto> getAllUsers() {
        return adminService.getAllUsers();
    }

    // ✅ API HEALTH MONITORING
    @GetMapping("/api-health")
    public List<ApiHealthDTO> getApiHealth() {
        return List.of(
            new ApiHealthDTO("/ip/search", "UP", "99.9%", "220ms"),
            new ApiHealthDTO("/patent/analyze", "DOWN", "98.1%", "—")
        );
    }
    
    @DeleteMapping("/users/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Integer id) {
        adminService.deleteUser(id);
        return ResponseEntity.ok("User deleted successfully");
    }
    
    
    @PutMapping("/users/{id}/verify")
    public ResponseEntity<String> verifyAdmin(@PathVariable Integer id) {
//        adminService.verifyAdmin(id);
        return ResponseEntity.ok("Admin verified");
    }
    
    
    // ✅ ADD THIS
    @GetMapping("/logs")
    public List<SystemLogDTO> getSystemLogs() {
        return logService.getRecentLogs();
    }
    
    
    // ✅ ADD THIS
    @GetMapping("/db-metrics")
    public Map<String, Object> getDbMetrics() {
        return dbMetricsService.getMetrics();
    }
    
    
    @GetMapping("/dashboard/admin")
    public Map<String, Object> getAdminDashboard() {

        Map<String, Object> response = new HashMap<>();

        response.put("systemStats", List.of(
            Map.of(
                "systemUsage", adminService.getSystemUsageTrends()
            )
        ));
        System.out.print(adminService.getSystemUsageTrends());

        return response;
    }

    
    
    


    
    
}
