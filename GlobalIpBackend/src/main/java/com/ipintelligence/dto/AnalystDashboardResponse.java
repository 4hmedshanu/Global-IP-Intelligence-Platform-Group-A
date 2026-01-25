package com.ipintelligence.dto;

import java.util.List;
import java.util.Map;

public class AnalystDashboardResponse {

    private List<Map<String, Object>> analyticsData;
    private List<Map<String, Object>> trendData;
    private List<Map<String, Object>> competitorActivity;
    private List<Map<String, Object>> subscriptions;
    private List<Map<String, Object>> recentFilings;
    private long activeSubscriptions;
    private long trackedTechnologies;


    // 🔥 Pie chart
    private List<Map<String, Object>> techPieData;

    // 🔥 Counts
    private long patentSearchCount;
    private long trademarkSearchCount;
    private long totalSearches;
   

    public AnalystDashboardResponse() {}
    
    
    public AnalystDashboardResponse(
            List<Map<String, Object>> analyticsData,
            List<Map<String, Object>> trendData,
            List<Map<String, Object>> competitorActivity,
            List<Map<String, Object>> subscriptions,
            List<Map<String, Object>> recentFilings,
            List<Map<String, Object>> techPieData,
            long patentSearchCount,
            long trademarkSearchCount,
            long totalSearches,
            long activeSubscriptions,
            long trackedTechnologies
    ) {
        this.analyticsData = analyticsData;
        this.trendData = trendData;
        this.competitorActivity = competitorActivity;
        this.subscriptions = subscriptions;
        this.recentFilings = recentFilings;
        this.techPieData = techPieData;
        this.patentSearchCount = patentSearchCount;
        this.trademarkSearchCount = trademarkSearchCount;
        this.totalSearches = totalSearches;
        this.activeSubscriptions = activeSubscriptions;
        this.trackedTechnologies = trackedTechnologies;
    }

    
    
    

   
    // ================= GETTERS =================

    public List<Map<String, Object>> getAnalyticsData() {
        return analyticsData;
    }

    public List<Map<String, Object>> getTrendData() {
        return trendData;
    }

    public List<Map<String, Object>> getCompetitorActivity() {
        return competitorActivity;
    }

    public List<Map<String, Object>> getSubscriptions() {
        return subscriptions;
    }

    public List<Map<String, Object>> getRecentFilings() {
        return recentFilings;
    }

    public List<Map<String, Object>> getTechPieData() {
        return techPieData;
    }

    public long getPatentSearchCount() {
        return patentSearchCount;
    }

    public long getTrademarkSearchCount() {
        return trademarkSearchCount;
    }

    public long getTotalSearches() {
        return totalSearches;
    }

    public long getActiveSubscriptions() {
        return activeSubscriptions;
    }

    public long getTrackedTechnologies() {
        return trackedTechnologies;
    }
}
