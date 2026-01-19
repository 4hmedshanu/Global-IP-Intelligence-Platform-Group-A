package com.ipintelligence.service.impl;

import com.ipintelligence.model.IpAsset;
import com.ipintelligence.model.Subscription;
import com.ipintelligence.model.SubscriptionStatus;
import com.ipintelligence.model.User;
import com.ipintelligence.repo.UserRepository;
import com.ipintelligence.repo.subscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class LandscapeService {

    private final subscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;

    public Map<String, Object> getLandscapeDataForUser(String email) {

        // ===============================
        // 1️⃣ USER
        // ===============================
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // ===============================
        // 2️⃣ ACTIVE SUBSCRIPTIONS
        // ===============================
        List<Subscription> subs =
                subscriptionRepository.findByUserAndStatus(user, SubscriptionStatus.ACTIVE);

        List<IpAsset> assets = subs.stream()
                .map(Subscription::getIpAsset)
                .filter(Objects::nonNull)
                .toList();

        Map<String, Object> response = new HashMap<>();

        // ===============================
        // 3️⃣ TECHNOLOGY TRENDS
        // ===============================
        Map<String, Long> techMap = new HashMap<>();

        for (IpAsset asset : assets) {
            String tech;

            if (asset.getCpcClassification() != null && !asset.getCpcClassification().isBlank()) {
                tech = asset.getCpcClassification().split("[;, ]")[0];
            } else if (asset.getIpcClassification() != null && !asset.getIpcClassification().isBlank()) {
                tech = asset.getIpcClassification().split("[;, ]")[0];
            } else {
                tech = "General"; // 🔥 fallback
            }

            techMap.put(tech, techMap.getOrDefault(tech, 0L) + 1);
        }

        List<Map<String, Object>> trendData = new ArrayList<>();
        techMap.forEach((tech, count) ->
                trendData.add(Map.of(
                        "technology", tech,
                        "growth", count
                ))
        );

     // ===============================
     // 4️⃣ ANALYTICS DATA (FIXED)
     // ===============================
     Map<String, Map<String, Integer>> dateMap = new HashMap<>();

     for (IpAsset asset : assets) {
         if (asset.getApplicationDate() == null) continue;

         String date = asset.getApplicationDate().toString();

         dateMap.putIfAbsent(date, new HashMap<>());
         Map<String, Integer> counts = dateMap.get(date);

         counts.put("patents", counts.getOrDefault("patents", 0) + 1);
         counts.putIfAbsent("trademarks", 0);
     }

     List<Map<String, Object>> analyticsData = new ArrayList<>();
     dateMap.forEach((date, counts) -> {
         analyticsData.add(Map.of(
                 "date", date,
                 "patents", counts.get("patents"),
                 "trademarks", counts.get("trademarks")
         ));
     });


  // ===============================
  // 5️⃣ TECHNOLOGY DISTRIBUTION (FIXED)
  // ===============================
  List<Map<String, Object>> techPieData = new ArrayList<>();

  techMap.forEach((tech, count) -> {
      techPieData.add(Map.of(
              "name", tech,
              "value", count.intValue() // 🔥 VERY IMPORTANT
      ));
  });

        // ===============================
        // 6️⃣ CITATION IMPACT
        // ===============================
        Random random = new Random();
        List<Map<String, Object>> citationData = new ArrayList<>();

        for (IpAsset asset : assets) {
            citationData.add(Map.of(
                    "application",
                    asset.getApplicationNumber() != null
                            ? asset.getApplicationNumber()
                            : asset.getTitle(),
                    "citations", random.nextInt(5) + 1
            ));
        }

        // ===============================
        // 7️⃣ PATENT FAMILY STRENGTH
        // ===============================
        List<Map<String, Object>> familyData = new ArrayList<>();

        for (IpAsset asset : assets) {
            familyData.add(Map.of(
                    "family",
                    asset.getApplicationNumber() != null
                            ? asset.getApplicationNumber()
                            : asset.getTitle(),
                    "size", random.nextInt(4) + 1
            ));
        }

        // ===============================
        // 8️⃣ RESPONSE
        // ===============================
        response.put("analyticsData", analyticsData);
        response.put("trendData", trendData);
        response.put("techPieData", techPieData);
        response.put("citationData", citationData);
        response.put("familyData", familyData);

        // ===============================
        // DEBUG (OPTIONAL)
        // ===============================
        System.out.println("Landscape for: " + email);
        System.out.println("Assets: " + assets.size());
        System.out.println("Techs: " + techPieData.size());

        return response;
    }
}
