package com.ipintelligence.dto;

import java.util.List;
import java.util.Map;

public class UserDashboardResponse {

    private int totalSearches;
    private int savedItems;
    private int activeAlerts;
    private int reports;
    private List<Map<String, Object>> activityData;
    private List<Map<String, Object>> technologyData;

    public UserDashboardResponse() {
    }

    public UserDashboardResponse(int totalSearches, int savedItems, int activeAlerts, int reports,
            List<Map<String, Object>> activityData, List<Map<String, Object>> technologyData) {
        this.totalSearches = totalSearches;
        this.savedItems = savedItems;
        this.activeAlerts = activeAlerts;
        this.reports = reports;
        this.activityData = activityData;
        this.technologyData = technologyData;
    }

    public int getTotalSearches() {
        return totalSearches;
    }

    public void setTotalSearches(int totalSearches) {
        this.totalSearches = totalSearches;
    }

    public int getSavedItems() {
        return savedItems;
    }

    public void setSavedItems(int savedItems) {
        this.savedItems = savedItems;
    }

    public int getActiveAlerts() {
        return activeAlerts;
    }

    public void setActiveAlerts(int activeAlerts) {
        this.activeAlerts = activeAlerts;
    }

    public int getReports() {
        return reports;
    }

    public void setReports(int reports) {
        this.reports = reports;
    }

    public List<Map<String, Object>> getActivityData() {
        return activityData;
    }

    public void setActivityData(List<Map<String, Object>> activityData) {
        this.activityData = activityData;
    }

    public List<Map<String, Object>> getTechnologyData() {
        return technologyData;
    }

    public void setTechnologyData(List<Map<String, Object>> technologyData) {
        this.technologyData = technologyData;
    }

    public static UserDashboardResponse mock(Object ignored) {
        return new UserDashboardResponse(
                324,
                42,
                12,
                8,
                List.of(
                        Map.of("month", "Jan", "searches", 45, "alerts", 12),
                        Map.of("month", "Feb", "searches", 52, "alerts", 18),
                        Map.of("month", "Mar", "searches", 48, "alerts", 15),
                        Map.of("month", "Apr", "searches", 61, "alerts", 22),
                        Map.of("month", "May", "searches", 55, "alerts", 19),
                        Map.of("month", "Jun", "searches", 67, "alerts", 25)
                ),
                List.of(
                        Map.of("name", "AI/ML", "value", 35, "color", "#4169e1"),
                        Map.of("name", "Blockchain", "value", 25, "color", "#3854c9"),
                        Map.of("name", "IoT", "value", 20, "color", "#4d5bff"),
                        Map.of("name", "Biotech", "value", 15, "color", "#3d40f5"),
                        Map.of("name", "Other", "value", 5, "color", "#6b88ff")
                )
        );
    }
}
