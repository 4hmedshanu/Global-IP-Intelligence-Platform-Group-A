package com.ipintelligence.service.impl;

import java.util.HashMap;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class DatabaseMetricsService {

	
	 private final JdbcTemplate jdbcTemplate;

	    public DatabaseMetricsService(JdbcTemplate jdbcTemplate) {
	        this.jdbcTemplate = jdbcTemplate;
	    }

	    public Map<String, Object> getMetrics() {

	        Map<String, Object> metrics = new HashMap<>();

	        metrics.put("Users",
	                jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Integer.class));

	        metrics.put("IP Assets",
	                jdbcTemplate.queryForObject("SELECT COUNT(*) FROM ip_assets", Integer.class));

	        metrics.put("Filings",
	                jdbcTemplate.queryForObject("SELECT COUNT(*) FROM filings", Integer.class));

	        metrics.put("Subscriptions",
	                jdbcTemplate.queryForObject("SELECT COUNT(*) FROM subscriptions", Integer.class));

	        metrics.put("Notifications",
	                jdbcTemplate.queryForObject("SELECT COUNT(*) FROM notifications", Integer.class));

	        metrics.put("Search History",
	                jdbcTemplate.queryForObject("SELECT COUNT(*) FROM search_history", Integer.class));

	        return metrics;
	    }
	    
}
