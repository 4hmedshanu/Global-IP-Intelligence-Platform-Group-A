package com.ipintelligence.service.impl;

import org.springframework.stereotype.Service;

import com.ipintelligence.dto.AnalystDashboardResponse;
import com.ipintelligence.model.User;
import com.ipintelligence.model.IpAsset;
import com.ipintelligence.repo.IpAssetRepository;
import com.ipintelligence.repo.SearchHistoryRepository;
import com.ipintelligence.service.AnalystDashboardService;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AnalystDashboardServiceImpl implements AnalystDashboardService {

    private final IpAssetRepository ipAssetRepository;
    private final SearchHistoryRepository searchHistoryRepository;

    public AnalystDashboardServiceImpl(IpAssetRepository ipAssetRepository, SearchHistoryRepository searchHistoryRepository) {
        this.ipAssetRepository = ipAssetRepository;
        this.searchHistoryRepository = searchHistoryRepository;
    }

    @Override
    public AnalystDashboardResponse getDashboardForAnalyst(User user, String jurisdiction, String technology, String fromDate, String toDate) {
        // 1. Analytics Data: filings per month (last 6 months)
        List<Map<String, Object>> analyticsData = new ArrayList<>();
        YearMonth now = YearMonth.now();
        for (int i = 5; i >= 0; i--) {
            YearMonth ym = now.minusMonths(i);
            LocalDate start = ym.atDay(1);
            LocalDate end = ym.atEndOfMonth();
            long patents = ipAssetRepository.searchWithFilters(
                    null, // title
                    null, // inventor
                    null, // assignee
                    jurisdiction != null && !jurisdiction.isEmpty() ? jurisdiction : null,
                    IpAsset.AssetType.PATENT,
                    null, // patentOffice
                    start,
                    end,
                    org.springframework.data.domain.Pageable.unpaged()
            ).getTotalElements();
            long trademarks = ipAssetRepository.searchWithFilters(
                    null, // title
                    null, // inventor
                    null, // assignee
                    jurisdiction != null && !jurisdiction.isEmpty() ? jurisdiction : null,
                    IpAsset.AssetType.TRADEMARK,
                    null, // patentOffice
                    start,
                    end,
                    org.springframework.data.domain.Pageable.unpaged()
            ).getTotalElements();
            long filings = patents + trademarks;
            Map<String, Object> entry = new HashMap<>();
            entry.put("date", ym.getMonth().toString().substring(0, 1) + ym.getMonth().toString().substring(1, 3).toLowerCase());
            entry.put("patents", patents);
            entry.put("trademarks", trademarks);
            entry.put("filings", filings);
            analyticsData.add(entry);
        }

        // 2. Trend Data: group by technology (keywords)
        LocalDate from = null;
        LocalDate to = null;
        try {
            if (fromDate != null && !fromDate.isEmpty()) {
                from = LocalDate.parse(fromDate);
            }
            if (toDate != null && !toDate.isEmpty()) {
                to = LocalDate.parse(toDate);
            }
        } catch (Exception ignored) {
        }

        List<IpAsset> assets = ipAssetRepository.searchWithFilters(
                null, // title
                null, // inventor
                null, // assignee
                jurisdiction != null && !jurisdiction.isEmpty() ? jurisdiction : null,
                null, // assetType
                null, // patentOffice
                from,
                to,
                org.springframework.data.domain.Pageable.unpaged()
        ).getContent();

        Map<String, Long> techMap = assets.stream()
                .filter(a -> a.getKeywords() != null && !a.getKeywords().isEmpty())
                .collect(Collectors.groupingBy(a -> a.getKeywords(), Collectors.counting()));
        List<Map<String, Object>> trendData = new ArrayList<>();
        for (Map.Entry<String, Long> e : techMap.entrySet()) {
            Map<String, Object> entry = new HashMap<>();
            entry.put("technology", e.getKey());
            entry.put("patents", e.getValue());
            entry.put("growth", (int) (Math.random() * 50));
            trendData.add(entry);
        }

        // 3. Competitor Activity: group by assignee/company
        Map<String, Long> companyMap = assets.stream()
                .filter(a -> a.getAssignee() != null && !a.getAssignee().isEmpty())
                .collect(Collectors.groupingBy(a -> a.getAssignee(), Collectors.counting()));
        List<Map<String, Object>> competitorActivity = new ArrayList<>();
        for (Map.Entry<String, Long> e : companyMap.entrySet()) {
            Map<String, Object> entry = new HashMap<>();
            entry.put("company", e.getKey());
            entry.put("filings", e.getValue());
            entry.put("grants", (int) (Math.random() * 30));
            entry.put("pending", (int) (Math.random() * 20));
            entry.put("trend", "+" + (int) (Math.random() * 20) + "%");
            competitorActivity.add(entry);
        }

        // 4. Subscriptions: mock for now
        List<Map<String, Object>> subscriptions = List.of(
                Map.of("id", 1, "keyword", "Artificial Intelligence", "jurisdiction", "US", "status", "Active", "newFilings", 12, "lastUpdate", "2h ago"),
                Map.of("id", 2, "keyword", "Blockchain Protocol", "jurisdiction", "EP", "status", "Active", "newFilings", 8, "lastUpdate", "5h ago"),
                Map.of("id", 3, "keyword", "IoT Security", "jurisdiction", "CN", "status", "Paused", "newFilings", 0, "lastUpdate", "1d ago")
        );

        // 5. Recent Filings: latest 5 assets
        List<Map<String, Object>> recentFilings = assets.stream()
                .sorted(Comparator.comparing(IpAsset::getApplicationDate, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(5)
                .map(a -> {
                    Map<String, Object> filing = new HashMap<>();
                    filing.put("title", a.getTitle());
                    filing.put("type", a.getAssetType() != null ? a.getAssetType().name() : "");
                    filing.put("jurisdiction", a.getJurisdiction());
                    filing.put("date", a.getApplicationDate() != null ? a.getApplicationDate().toString() : "");
                    return filing;
                })
                .collect(Collectors.toList());

        // Brute-force: count all searches for the user (ignoring dataSource)
        long totalSearchCount = 0;
        if (user != null) {
            totalSearchCount = searchHistoryRepository.countByUser(user);
            System.out.println("[ANALYST-DASHBOARD-DEBUG] user.id=" + user.getId() + ", user.email=" + user.getEmail() + ", countByUser=" + totalSearchCount);
        } else {
            System.out.println("[ANALYST-DASHBOARD-DEBUG] user is null");
        }

        // Debug log for backend count
        System.out.println("[ANALYST-DASHBOARD] Returning search count for user " + (user != null ? user.getId() : "null") + ": " + totalSearchCount);

        // For demo: show totalSearchCount in both badges, but also return totalSearches
        long patentSearchCount = totalSearchCount;
        long trademarkSearchCount = totalSearchCount;
        long totalSearches = totalSearchCount;

        return new AnalystDashboardResponse(
                analyticsData,
                trendData,
                competitorActivity,
                subscriptions,
                recentFilings,
                patentSearchCount,
                trademarkSearchCount,
                totalSearches
        );
    }
}
