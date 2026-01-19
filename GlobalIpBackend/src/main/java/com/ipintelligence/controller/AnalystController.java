package com.ipintelligence.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/analyst")
@PreAuthorize("hasRole('ANALYST')")
public class AnalystController {

    @GetMapping
    //returns to frontend
    public ResponseEntity<String> getAnalystData() {
        return ResponseEntity.ok("✅ ANALYST access granted! Advanced analytics available.");
    }
}
