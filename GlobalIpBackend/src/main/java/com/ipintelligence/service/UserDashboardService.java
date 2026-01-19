package com.ipintelligence.service;

import com.ipintelligence.dto.UserDashboardResponse;
import com.ipintelligence.model.User;

public interface UserDashboardService {

    UserDashboardResponse getDashboardForUser(User user);
}
