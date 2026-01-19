package com.ipintelligence.service.impl;

import com.ipintelligence.dto.AdminDashboardResponse;
import com.ipintelligence.dto.SystemUsageDTO;
import com.ipintelligence.dto.UserDto;
import com.ipintelligence.model.User;
import com.ipintelligence.repo.UserRepository;
import com.ipintelligence.repo.SearchHistoryRepository;
import com.ipintelligence.repo.IpAssetRepository;
import com.ipintelligence.service.AdminDashboardService;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;



@Service
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private final UserRepository userRepository;
    private final SearchHistoryRepository searchHistoryRepository;
    private final IpAssetRepository ipAssetRepository;

    @Autowired
    public AdminDashboardServiceImpl(UserRepository userRepository, SearchHistoryRepository searchHistoryRepository, IpAssetRepository ipAssetRepository) {
        this.userRepository = userRepository;
        this.searchHistoryRepository = searchHistoryRepository;
        this.ipAssetRepository = ipAssetRepository;
    }

    @Override
    public AdminDashboardResponse getDashboardForAdmin(User user) {
        // User stats: total users, active users, new users last 7 days
        long totalUsers = userRepository.count();
        long activeUsers = userRepository.findAll().stream().filter(u -> u.getLastName() != null).count(); // Example: lastName as activity
        long newUsers = userRepository.findAll().stream().filter(u -> u.getCreatedAt().isAfter(LocalDateTime.now().minusDays(7))).count();
        List<Map<String, Object>> userStats = List.of(
                Map.of("totalUsers", totalUsers, "activeUsers", activeUsers, "newUsers", newUsers)
        );

        // System stats: total searches, total assets
        long totalSearches = searchHistoryRepository.count();
        long totalAssets = ipAssetRepository.countAllAssets();
        List<Map<String, Object>> systemStats = List.of(
                Map.of("totalSearches", totalSearches, "totalAssets", totalAssets)
        );

        // Recent activity: last 5 searches
        List<Map<String, Object>> recentActivity = new ArrayList<>();
        searchHistoryRepository.findAll().stream()
                .sorted(Comparator.comparing(s -> s.getCreatedAt(), Comparator.reverseOrder()))
                .limit(5)
                .forEach(s -> recentActivity.add(Map.of(
                "user", s.getUser().getEmail(),
                "query", s.getSearchQuery(),
                "type", s.getSearchType(),
                "date", s.getCreatedAt()
        )));

        return new AdminDashboardResponse(userStats, systemStats, recentActivity);
    }
    
    
    public List<UserDto> getAllUsers() {

        List<User> users = userRepository.findAll();

        return users.stream()
                .map(user -> new UserDto(
                        user.getId(),
                        user.getUsername(),
                        user.getEmail(),
                        user.getRole(),
                        user.getCreatedAt()
                ))
                .toList();
    }
    
    
    
    
//    public void deleteUser(Integer id) {
//        User user = userRepository.findById(id).orElse(null);
//        if(user==null) {
//        	
//        }
//        else {
//        	userRepository.delete(user);
//        }
//        
//    }
    @Transactional
    public void deleteUser(Integer id) {
        searchHistoryRepository.deleteByUserId(id); // 👈 delete children first
        userRepository.deleteById(id);
    }

   
//    public void verifyAdmin(Integer id) {
//        User user = userRepository.findById(id)
//            .orElseThrow(() -> new RuntimeException("User not found"));
//
//        if (!"ADMIN".equals(user.getRole())) {
//            throw new RuntimeException("Not an admin user");
//        }
//
//        user.setVerified(true);
//        userRepository.save(user);
//    }
    
    
    public List<SystemUsageDTO> getSystemUsageTrends() {

        Map<String, SystemUsageDTO> map = new LinkedHashMap<>();

        // users per day
        for (Object[] row : userRepository.countUsersPerDay()) {
            String date = row[0].toString();
            Long count = (Long) row[1];
            map.put(date, new SystemUsageDTO(date, count, 0L));
        }

        // api calls per day
        for (Object[] row : searchHistoryRepository.countApiCallsPerDay()) {
            String date = row[0].toString();
            Long count = (Long) row[1];

            map.computeIfAbsent(
                date,
                d -> new SystemUsageDTO(d, 0L, 0L)
            ).setApiCalls(count);
        }

        return new ArrayList<>(map.values());
    }


}
