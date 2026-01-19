package com.ipintelligence.dto;

import java.util.List;
import java.util.Map;

public class AdminDashboardResponse {

    private List<Map<String, Object>> userStats;
    private List<Map<String, Object>> systemStats;
    private List<Map<String, Object>> recentActivity;

    public AdminDashboardResponse() {
    }

    public AdminDashboardResponse(List<Map<String, Object>> userStats,
            List<Map<String, Object>> systemStats,
            List<Map<String, Object>> recentActivity) {
        this.userStats = userStats;
        this.systemStats = systemStats;
        this.recentActivity = recentActivity;
    }

    public List<Map<String, Object>> getUserStats() {
        return userStats;
    }

    public void setUserStats(List<Map<String, Object>> userStats) {
        this.userStats = userStats;
    }

    public List<Map<String, Object>> getSystemStats() {
        return systemStats;
    }

    public void setSystemStats(List<Map<String, Object>> systemStats) {
        this.systemStats = systemStats;
    }

    public List<Map<String, Object>> getRecentActivity() {
        return recentActivity;
    }

    public void setRecentActivity(List<Map<String, Object>> recentActivity) {
        this.recentActivity = recentActivity;
    }
}
