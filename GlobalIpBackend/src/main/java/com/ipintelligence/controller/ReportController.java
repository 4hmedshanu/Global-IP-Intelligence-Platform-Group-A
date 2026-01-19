package com.ipintelligence.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ipintelligence.dto.StatusMetricDTO;
import com.ipintelligence.service.impl.ReportService;

@RestController
@RequestMapping("/api/reports")

public class ReportController {

	
	@Autowired
     ReportService reportService;

    

    @GetMapping("/status-metrics")
    public ResponseEntity<List<StatusMetricDTO>> getStatusMetrics() {
        return ResponseEntity.ok(reportService.getLifecycleStatusMetrics());
    }
}
