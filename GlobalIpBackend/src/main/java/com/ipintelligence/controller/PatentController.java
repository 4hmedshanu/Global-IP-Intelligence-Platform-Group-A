package com.ipintelligence.controller;

import com.ipintelligence.dto.SearchRequestDto;
import com.ipintelligence.dto.SearchResultDto;
import com.ipintelligence.service.GooglePatentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/patents")
@Slf4j
@CrossOrigin(origins = "*")
public class PatentController {

    @Autowired
    private GooglePatentService googlePatentService;

    /**
     * Search patents using BigQuery
     */
    @PostMapping("/search")
    public ResponseEntity<SearchResultDto> searchPatents(@RequestBody SearchRequestDto searchRequest) {
        try {
            log.info("Patent search request received: {}", searchRequest.getQuery());

            // Validate search request
            if (searchRequest.getQuery() == null || searchRequest.getQuery().trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            // Set defaults if not provided (page and size are primitive int, default to 0)
            if (searchRequest.getPage() < 0) {
                searchRequest.setPage(0);
            }
            if (searchRequest.getSize() <= 0) {
                searchRequest.setSize(20);
            }

            SearchResultDto result = googlePatentService.search(searchRequest);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("Error searching patents", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get patent details by publication number
     */
    @GetMapping("/{publicationNumber}")
    public ResponseEntity<?> getPatentDetails(@PathVariable String publicationNumber) {
        try {
            log.info("Fetching patent details for: {}", publicationNumber);

            var patentDetails = googlePatentService.getAssetDetails(publicationNumber);

            if (patentDetails == null) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(patentDetails);

        } catch (Exception e) {
            log.error("Error fetching patent details", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Check if BigQuery service is available
     */
    @GetMapping("/health")
    public ResponseEntity<String> checkHealth() {
        try {
            boolean available = googlePatentService.isAvailable();
            if (available) {
                return ResponseEntity.ok("BigQuery Patent Service is available");
            } else {
                return ResponseEntity.status(503).body("BigQuery Patent Service is unavailable");
            }
        } catch (Exception e) {
            log.error("Health check failed", e);
            return ResponseEntity.status(503).body("BigQuery Patent Service error: " + e.getMessage());
        }
    }
}
