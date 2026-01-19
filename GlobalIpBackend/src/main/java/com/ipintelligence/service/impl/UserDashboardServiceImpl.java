package com.ipintelligence.service.impl;

import com.ipintelligence.dto.UserDashboardResponse;
import com.ipintelligence.model.IpAsset;
import com.ipintelligence.model.User;
import com.ipintelligence.repo.IpAssetRepository;
import com.ipintelligence.repo.SearchHistoryRepository;
import com.ipintelligence.service.UserDashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserDashboardServiceImpl implements UserDashboardService {

    private final SearchHistoryRepository searchHistoryRepository;
    private final IpAssetRepository ipAssetRepository;

    @Autowired
    public UserDashboardServiceImpl(SearchHistoryRepository searchHistoryRepository, IpAssetRepository ipAssetRepository) {
        this.searchHistoryRepository = searchHistoryRepository;
        this.ipAssetRepository = ipAssetRepository;
    }

    @Override
    public UserDashboardResponse getDashboardForUser(User user) {
        System.out.println("[DASHBOARD-DEBUG] Aggregating dashboard for user: " + (user != null ? user.getId() + ", " + user.getEmail() : "null"));
        int totalSearches = (int) searchHistoryRepository.countByUser(user);
        // For demo: savedItems, activeAlerts, reports are random or 0 unless you have models for them
        int savedItems = 0; // TODO: implement if you have a SavedItem entity
        int activeAlerts = 0; // TODO: implement if you have an Alert entity
        int reports = 0; // TODO: implement if you have a Report entity

        // Activity Data: searches per month (last 6 months)
        List<Map<String, Object>> activityData = new ArrayList<>();
        YearMonth now = YearMonth.now();
        for (int i = 5; i >= 0; i--) {
            YearMonth ym = now.minusMonths(i);
            LocalDate start = ym.atDay(1);
            LocalDate end = ym.atEndOfMonth();
            long count = searchHistoryRepository.findByUserAndCreatedAtBetween(user, start.atStartOfDay(), end.atTime(23, 59, 59)).size();
            Map<String, Object> entry = new HashMap<>();
            entry.put("month", ym.getMonth().toString().substring(0, 1) + ym.getMonth().toString().substring(1, 3).toLowerCase());
            entry.put("searches", count);
            activityData.add(entry);
        }

        // Technology Data: distribution by technology (if you have a way to tag searches or assets)
        // For demo, group user's assets by keywords (if available)
        List<IpAsset> userAssets = new ArrayList<>();
        // If you have a way to link assets to user, use that. Otherwise, leave empty or mock.
        List<Map<String, Object>> technologyData = new ArrayList<>();
        // TODO: implement real technology distribution if possible

        return new UserDashboardResponse(
                totalSearches,
                savedItems,
                activeAlerts,
                reports,
                activityData,
                technologyData
        );
    }
}
