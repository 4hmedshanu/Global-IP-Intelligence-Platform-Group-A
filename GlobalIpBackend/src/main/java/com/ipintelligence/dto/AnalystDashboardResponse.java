package com.ipintelligence.dto;

import java.util.List;
import java.util.Map;

public class AnalystDashboardResponse {

    private List<Map<String, Object>> analyticsData;
    private List<Map<String, Object>> trendData;
    private List<Map<String, Object>> competitorActivity;
    private List<Map<String, Object>> subscriptions;
    private List<Map<String, Object>> recentFilings;

    // New fields for search counts
    private long patentSearchCount;
    private long trademarkSearchCount;
    private long totalSearches;

    public AnalystDashboardResponse() {
    }

    public AnalystDashboardResponse(List<Map<String, Object>> analyticsData,
            List<Map<String, Object>> trendData,
            List<Map<String, Object>> competitorActivity,
            List<Map<String, Object>> subscriptions,
            List<Map<String, Object>> recentFilings,
            long patentSearchCount,
            long trademarkSearchCount,
            long totalSearches) {
        this.analyticsData = analyticsData;
        this.trendData = trendData;
        this.competitorActivity = competitorActivity;
        this.subscriptions = subscriptions;
        this.recentFilings = recentFilings;
        this.patentSearchCount = patentSearchCount;
        this.trademarkSearchCount = trademarkSearchCount;
        this.totalSearches = totalSearches;
    }

    public long getTotalSearches() {
        return totalSearches;
    }

    public void setTotalSearches(long totalSearches) {
        this.totalSearches = totalSearches;
    }

    public List<Map<String, Object>> getAnalyticsData() {
        return analyticsData;
    }

    public void setAnalyticsData(List<Map<String, Object>> analyticsData) {
        this.analyticsData = analyticsData;
    }

    public List<Map<String, Object>> getTrendData() {
        return trendData;
    }

    public void setTrendData(List<Map<String, Object>> trendData) {
        this.trendData = trendData;
    }

    public List<Map<String, Object>> getCompetitorActivity() {
        return competitorActivity;
    }

    public void setCompetitorActivity(List<Map<String, Object>> competitorActivity) {
        this.competitorActivity = competitorActivity;
    }

    public List<Map<String, Object>> getSubscriptions() {
        return subscriptions;
    }

    public void setSubscriptions(List<Map<String, Object>> subscriptions) {
        this.subscriptions = subscriptions;
    }

    public List<Map<String, Object>> getRecentFilings() {
        return recentFilings;
    }

    public void setRecentFilings(List<Map<String, Object>> recentFilings) {
        this.recentFilings = recentFilings;
    }

    public long getPatentSearchCount() {
        return patentSearchCount;
    }

    public void setPatentSearchCount(long patentSearchCount) {
        this.patentSearchCount = patentSearchCount;
    }

    public long getTrademarkSearchCount() {
        return trademarkSearchCount;
    }

    public void setTrademarkSearchCount(long trademarkSearchCount) {
        this.trademarkSearchCount = trademarkSearchCount;
    }

    public static AnalystDashboardResponse mock(Object ignored) {
        return new AnalystDashboardResponse(
                List.of(
                        Map.of("date", "Jan", "patents", 450, "trademarks", 320, "filings", 180),
                        Map.of("date", "Feb", "patents", 520, "trademarks", 380, "filings", 210),
                        Map.of("date", "Mar", "patents", 610, "trademarks", 420, "filings", 240),
                        Map.of("date", "Apr", "patents", 580, "trademarks", 450, "filings", 220),
                        Map.of("date", "May", "patents", 700, "trademarks", 510, "filings", 280),
                        Map.of("date", "Jun", "patents", 780, "trademarks", 580, "filings", 320)
                ),
                List.of(
                        Map.of("technology", "AI/ML", "growth", 45, "patents", 234),
                        Map.of("technology", "Blockchain", "growth", 32, "patents", 189),
                        Map.of("technology", "IoT", "growth", 28, "patents", 156),
                        Map.of("technology", "Biotech", "growth", 38, "patents", 198),
                        Map.of("technology", "Quantum", "growth", 52, "patents", 87)
                ),
                List.of(
                        Map.of("company", "TechCorp", "filings", 45, "grants", 28, "pending", 17, "trend", "+12%"),
                        Map.of("company", "InnovateLabs", "filings", 38, "grants", 22, "pending", 16, "trend", "+8%"),
                        Map.of("company", "FutureSystems", "filings", 32, "grants", 19, "pending", 13, "trend", "+15%")
                ),
                List.of(
                        Map.of("id", 1, "keyword", "Artificial Intelligence", "jurisdiction", "US", "status", "Active", "newFilings", 12, "lastUpdate", "2h ago"),
                        Map.of("id", 2, "keyword", "Blockchain Protocol", "jurisdiction", "EP", "status", "Active", "newFilings", 8, "lastUpdate", "5h ago"),
                        Map.of("id", 3, "keyword", "IoT Security", "jurisdiction", "CN", "status", "Paused", "newFilings", 0, "lastUpdate", "1d ago")
                ),
                List.of(
                        Map.of("title", "AI Patent Filing", "type", "PATENT", "jurisdiction", "US", "date", "2025-06-01"),
                        Map.of("title", "Blockchain Trademark", "type", "TRADEMARK", "jurisdiction", "EP", "date", "2025-05-15"),
                        Map.of("title", "IoT Patent Filing", "type", "PATENT", "jurisdiction", "CN", "date", "2025-04-20"),
                        Map.of("title", "Biotech Patent Filing", "type", "PATENT", "jurisdiction", "US", "date", "2025-03-10"),
                        Map.of("title", "Quantum Patent Filing", "type", "PATENT", "jurisdiction", "EP", "date", "2025-02-05")
                ),
                0L,
                0L,
                0L // totalSearches for mock
        );
    }
}
