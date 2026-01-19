package com.ipintelligence.service;

import com.ipintelligence.dto.AnalystDashboardResponse;
import com.ipintelligence.model.User;

public interface AnalystDashboardService {
    AnalystDashboardResponse getDashboardForAnalyst(
        User user,
        String jurisdiction,
        String technology,
        String fromDate,
        String toDate
    );
}
