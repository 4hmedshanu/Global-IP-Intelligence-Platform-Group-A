package com.ipintelligence.service;

import com.ipintelligence.dto.AdminDashboardResponse;
import com.ipintelligence.model.User;

public interface AdminDashboardService {

    AdminDashboardResponse getDashboardForAdmin(User user);
}
