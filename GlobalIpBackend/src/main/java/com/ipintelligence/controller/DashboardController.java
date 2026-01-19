package com.ipintelligence.controller;

import com.ipintelligence.dto.UserDashboardResponse;
import com.ipintelligence.dto.AnalystDashboardResponse;
import com.ipintelligence.service.UserDashboardService;
import com.ipintelligence.service.AnalystDashboardService;
import com.ipintelligence.service.AdminDashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import com.ipintelligence.model.User;
import com.ipintelligence.repo.UserRepository;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "http://localhost:3000")

public class DashboardController {

    private final UserDashboardService userDashboardService;
    private final AnalystDashboardService analystDashboardService;
    private final AdminDashboardService adminDashboardService;
    private final UserRepository userRepository;

    public DashboardController(UserDashboardService userDashboardService, AnalystDashboardService analystDashboardService, AdminDashboardService adminDashboardService, UserRepository userRepository) {
        this.userDashboardService = userDashboardService;
        this.analystDashboardService = analystDashboardService;
        this.adminDashboardService = adminDashboardService;
        this.userRepository = userRepository;
    }

    @GetMapping("/admin")
    public ResponseEntity<com.ipintelligence.dto.AdminDashboardResponse> getAdminDashboard(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(adminDashboardService.getDashboardForAdmin(user));
    }

    @GetMapping("/user")
    public ResponseEntity<UserDashboardResponse> getUserDashboard(@AuthenticationPrincipal org.springframework.security.core.userdetails.User principal) {
        User realUser = null;
        if (principal != null && principal.getUsername() != null) {
            realUser = userRepository.findByEmail(principal.getUsername()).orElse(null);
        }
        return ResponseEntity.ok(userDashboardService.getDashboardForUser(realUser));
    }

    @GetMapping("/analyst")
    public ResponseEntity<AnalystDashboardResponse> getAnalystDashboard(
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User principal,
            @RequestParam(value = "jurisdiction", required = false) String jurisdiction,
            @RequestParam(value = "technology", required = false) String technology,
            @RequestParam(value = "fromDate", required = false) String fromDate,
            @RequestParam(value = "toDate", required = false) String toDate
    ) {
        User realUser = null;
        if (principal != null && principal.getUsername() != null) {
            realUser = userRepository.findByEmail(principal.getUsername()).orElse(null);
        }
        return ResponseEntity.ok(
            analystDashboardService.getDashboardForAnalyst(realUser, jurisdiction, technology, fromDate, toDate)
        );
    }
    
    
    
}




