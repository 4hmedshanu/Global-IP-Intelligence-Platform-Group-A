package com.ipintelligence.service.impl;

import com.ipintelligence.dto.UserDashboardResponse;
import com.ipintelligence.model.User;
import com.ipintelligence.repo.IpAssetRepository;
import com.ipintelligence.repo.SearchHistoryRepository;
import com.ipintelligence.repo.subscriptionRepository;
import com.ipintelligence.service.UserDashboardService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

@Service
public class UserDashboardServiceImpl implements UserDashboardService {

    private final SearchHistoryRepository searchHistoryRepository;
    private final IpAssetRepository ipAssetRepository;
    private final subscriptionRepository subscriptionRepository;

    // ✅ SINGLE constructor injection (BEST PRACTICE)
    public UserDashboardServiceImpl(
            SearchHistoryRepository searchHistoryRepository,
            IpAssetRepository ipAssetRepository,
            subscriptionRepository subscriptionRepository
    ) {
        this.searchHistoryRepository = searchHistoryRepository;
        this.ipAssetRepository = ipAssetRepository;
        this.subscriptionRepository = subscriptionRepository;
    }

    @Override
    public UserDashboardResponse getDashboardForUser(User user) {

        if (user == null) {
            return new UserDashboardResponse();
        }

        int totalSearches = (int) searchHistoryRepository.countByUser(user);

        // ✅ FIXED: Saved Items = total subscriptions
        int savedItems = (int) subscriptionRepository.countByUser(user);

        int activeAlerts = 0;
        int reports = 0;

        // Activity Data (last 6 months)
        List<Map<String, Object>> activityData = new ArrayList<>();
        YearMonth now = YearMonth.now();

        for (int i = 5; i >= 0; i--) {
            YearMonth ym = now.minusMonths(i);
            LocalDate start = ym.atDay(1);
            LocalDate end = ym.atEndOfMonth();

            long count = searchHistoryRepository
                    .findByUserAndCreatedAtBetween(
                            user,
                            start.atStartOfDay(),
                            end.atTime(23, 59, 59)
                    ).size();

            Map<String, Object> entry = new HashMap<>();
            entry.put("month", ym.getMonth().toString().substring(0, 3));
            entry.put("searches", count);
            activityData.add(entry);
        }

        List<Map<String, Object>> technologyData = new ArrayList<>();

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
