
package com.ipintelligence.service.impl;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.ipintelligence.model.IpAsset;
import com.ipintelligence.model.Subscription;
import com.ipintelligence.repo.IpAssetRepository;
import com.ipintelligence.repo.subscriptionRepository;

@Service
public class FilingTrackerService {

    @Autowired private subscriptionRepository subRepo;
    @Autowired private IpAssetRepository assetRepo;
    @Autowired private SimpMessagingTemplate messagingTemplate;

    @Scheduled(cron = "0 0 0 * * *")
    public void trackStatusChanges() {

        List<Subscription> allSubs = subRepo.findAll();

        for (Subscription sub : allSubs) {

            IpAsset asset = sub.getIpAsset();
            String apiStatus = fetchStatusFromExternalAPI(asset.getExternalId());

            if (!apiStatus.equals(asset.getStatus())) {

                asset.setStatus(apiStatus);

                String lifecycle = calculateLifecycle(asset);
                asset.setLegalStatus(lifecycle);

                assetRepo.save(asset);

                Map<String, String> alert = new HashMap<>();
                alert.put("title", asset.getTitle());
                alert.put("newStatus", apiStatus);
                alert.put("lifecycle", lifecycle);

                messagingTemplate.convertAndSend("/topic/alerts", alert);
            }
        }
    }

    private String fetchStatusFromExternalAPI(String externalId) {
        return "Granted"; // mock
    }

    public String calculateLifecycle(IpAsset asset) {

        LocalDate today = LocalDate.now();

        if (asset.getExpiryDate() != null &&
            asset.getExpiryDate().isBefore(today)) {
            return "EXPIRED";
        }

        if (asset.getExpiryDate() != null &&
            asset.getExpiryDate().minusYears(1).isBefore(today)) {
            return "RENEWAL";
        }

        if (asset.getGrantDate() != null) {
            return "GRANTED";
        }

        if (asset.getApplicationDate() != null) {
            return "APPLICATION";
        }

        return "UNKNOWN";
    }
}