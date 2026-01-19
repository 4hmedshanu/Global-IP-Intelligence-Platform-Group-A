package com.ipintelligence.controller;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.ipintelligence.dto.FilingDto;
import com.ipintelligence.dto.IpAssetDto;
import com.ipintelligence.dto.Subscriptiondto;
import com.ipintelligence.model.Filing;
import com.ipintelligence.model.IpAsset;
import com.ipintelligence.model.Subscription;
import com.ipintelligence.model.SubscriptionStatus;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import com.ipintelligence.model.User;
import com.ipintelligence.repo.IpAssetRepository;
import com.ipintelligence.repo.UserRepository;
import com.ipintelligence.repo.filingRepository;
import com.ipintelligence.repo.subscriptionRepository;
import com.ipintelligence.service.SubscriptionService;
import com.ipintelligence.service.impl.FilingTrackerService;


@RestController
@RequestMapping("/api/tracker")
public class TrackerController {
	
	
	@Autowired
	subscriptionRepository subscriptionRepository;
	
	@Autowired
	IpAssetRepository ipAssetRepository;
	
	@Autowired
	UserRepository userRepo;
	
	@Autowired
	filingRepository filingRepository ;
	
	@Autowired
	SubscriptionService subscriptionService;
	
	
	
	@Autowired
	FilingTrackerService  filingtrackerservices;
	

    
    
    
    
    @GetMapping("/my")
    public ResponseEntity<Map<String, Object>> getMyTrackedAssets(Principal principal) {

        User user = userRepo.findByEmail(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

       
        
        
        List<Subscription> subs =
        	    subscriptionRepository.findByUser(user);
        

        List<Map<String, Object>> assets = new ArrayList<>();

        long application = 0, granted = 0, renewal = 0, expired = 0;

        for (Subscription s : subs) {
            IpAsset asset = s.getIpAsset();

            // 🔥 lifecycle calculate
            String lifecycle = filingtrackerservices.calculateLifecycle(asset);
            asset.setLegalStatus(lifecycle);

            switch (lifecycle) {
                case "APPLICATION" -> application++;
                case "GRANTED" -> granted++;
                case "RENEWAL" -> renewal++;
                case "EXPIRED" -> expired++;
            }

            Map<String, Object> assetMap = new HashMap<>();
            assetMap.put("id", asset.getId());
            assetMap.put("title", asset.getTitle());
            assetMap.put("status", lifecycle);
            assetMap.put("jurisdiction", asset.getJurisdiction());
            assetMap.put(
                "filingDate",
                asset.getApplicationDate() != null
                    ? asset.getApplicationDate().toString()
                    : null
            );

            assets.add(assetMap);


            
            
            
        }

        Map<String, Object> response = new HashMap<>();
        response.put("assets", assets);
        response.put("statusSummary", Map.of(
                "application", application,
                "granted", granted,
                "renewal", renewal,
                "expired", expired
        ));

        return ResponseEntity.ok(response);
    }

    
    
    
    @PostMapping("/subscribe")
    public ResponseEntity<Map<String, String>> subscribeAsset(
            @RequestBody IpAsset asset,
            Principal principal
    ) {

        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("status", "ERROR", "message", "User not authenticated"));
        }

        // 1️⃣ Validate externalId
        if (asset.getExternalId() == null || asset.getExternalId().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("status", "ERROR", "message", "ExternalId is required"));
        }

        // 2️⃣ Get User safely
        User user = userRepo.findByEmail(principal.getName())
                .orElseThrow(() ->
                        new RuntimeException("User not found: " + principal.getName())
                );

        // 3️⃣ Get or save asset safely
        IpAsset savedAsset = ipAssetRepository
                .findByExternalId(asset.getExternalId())
                .orElseGet(() -> ipAssetRepository.save(asset));

        // 4️⃣ Prevent duplicate subscription (IMPORTANT)
        boolean alreadySubscribed = subscriptionRepository
                .existsByUserAndIpAsset(user, savedAsset);

        if (alreadySubscribed) {
            return ResponseEntity.ok(
                    Map.of("status", "INFO", "message", "Already subscribed")
            );
        }

        // 5️⃣ Save subscription
        Subscription subscription = new Subscription();
        subscription.setUser(user);
        subscription.setIpAsset(savedAsset);
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setCreatedAt(LocalDateTime.now());

        subscriptionRepository.save(subscription);
     // 6️⃣ Create initial filing record
        try {
        	Filing filing = new Filing();
            filing.setIpAsset(savedAsset);
            filing.setStatus("SUBSCRIBED");
            filing.setDescription("Asset monitoring started");
            filing.setDate(LocalDateTime.now());
            filingRepository.save(filing);
        }catch (Exception e) {
			// TODO: handle exception
        	System.out.print(e.getMessage());
		}

        

        return ResponseEntity.ok(
                Map.of("status", "SUCCESS", "message", "Asset subscribed successfully")
        );
    }
    
    

    
    @PostMapping("/unsubscribe")
    public ResponseEntity<?> unsubscribe(
            @RequestBody Map<String, String> body,
            Principal principal
    ) {
        String externalId = body.get("externalId");

        User user = userRepo.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        IpAsset ipAsset = ipAssetRepository.findByExternalId(externalId)
                .orElseThrow(() -> new RuntimeException("IP Asset not found"));

        subscriptionService.unsubscribe(user, ipAsset);

        return ResponseEntity.ok("Unsubscribed successfully");
    }


    
    
    @GetMapping("/subscriptions")
    public List<Subscriptiondto> getAllSubscriptions() {
        return subscriptionRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    
    @GetMapping("/subscriptionsbyid")
    public ResponseEntity<List<Subscriptiondto>> getAllSubscriptionsByUser(
            Principal principal
    ) {

        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User user = userRepo.findByEmail(principal.getName())
                .orElseThrow(() ->
                        new RuntimeException("User not found: " + principal.getName())
                );

        List<Subscription> subs = subscriptionRepository.findByUser(user);

        List<Subscriptiondto> response = subs.stream()
                .map(this::mapToDto)
                .toList();

        return ResponseEntity.ok(response);
    }
    
    // ================= MAPPERS =================

    private Subscriptiondto mapToDto(Subscription sub) {

        Subscriptiondto dto = new Subscriptiondto();
        dto.setId(sub.getId());
        dto.setCreatedAt(sub.getCreatedAt());

        // USER
        if (sub.getUser() != null) {
            dto.setUserId(sub.getUser().getId());
            dto.setUsername(sub.getUser().getUsername());
            dto.setEmail(sub.getUser().getEmail());
        }

        // IP ASSET (FULL)
        if (sub.getIpAsset() != null) {
            dto.setIpAsset(mapIpAssetToDto(sub.getIpAsset()));
        }

        return dto;
    }

    private IpAssetDto mapIpAssetToDto(IpAsset asset) {

        IpAssetDto dto = new IpAssetDto();

        dto.setId(asset.getId());
        dto.setExternalId(asset.getExternalId());
        dto.setApplicationNumber(asset.getApplicationNumber());
        dto.setPublicationNumber(asset.getPublicationNumber());

        dto.setTitle(asset.getTitle());
        dto.setDescription(asset.getDescription());
        dto.setAssetType(asset.getAssetType());
        dto.setStatus(asset.getStatus());
        dto.setJurisdiction(asset.getJurisdiction());
        dto.setPatentOffice(asset.getPatentOffice());

        dto.setInventor(asset.getInventor());
        dto.setAssignee(asset.getAssignee());

        dto.setPriorityDate(asset.getPriorityDate());
        dto.setApplicationDate(asset.getApplicationDate());
        dto.setPublicationDate(asset.getPublicationDate());
        dto.setGrantDate(asset.getGrantDate());
        dto.setExpiryDate(asset.getExpiryDate());

        dto.setIpcClassification(asset.getIpcClassification());
        dto.setCpcClassification(asset.getCpcClassification());

        // Optional derived field
        dto.setClassification(
                asset.getCpcClassification() + " | " + asset.getIpcClassification()
        );

        dto.setKeywords(asset.getKeywords());
        dto.setLegalStatus(asset.getLegalStatus());

        dto.setCreatedAt(asset.getCreatedAt());
        dto.setUpdatedAt(asset.getUpdatedAt());

        // Optional: URL for frontend
        dto.setUrl("/api/ip-assets/" + asset.getId());

        return dto;
    }
    
    
    @PostMapping("/toggle")
    public ResponseEntity<?> toggleSubscription(
            @RequestBody Map<String, Integer> body,
            Principal principal
    ) {
        int subId = body.get("subscriptionId");

        User user = userRepo.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Subscription sub = subscriptionRepository.findById(subId)
                .orElseThrow(() -> new RuntimeException("Subscription not found"));

        if (!sub.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // 🔁 TOGGLE
        if (sub.getStatus() == SubscriptionStatus.ACTIVE) {
            sub.setStatus(SubscriptionStatus.PAUSED);
        } else {
            sub.setStatus(SubscriptionStatus.ACTIVE);
        }


        subscriptionRepository.save(sub);

        return ResponseEntity.ok(
                Map.of("status", sub.getStatus())
        );
    }

    
    
    
    
    
    
    
    
    
    /// 
 // IP Asset ki poori history (Modal ke liye) 
//    @GetMapping("/filings/{assetId}")
//    public ResponseEntity<List<Filing>> getFilingHistory(@PathVariable Long assetId) {
//        return ResponseEntity.ok(filingRepository.findByIpAssetIdOrderByDateDesc(assetId));
//    }
    
    
    @GetMapping("/filings/{assetId}")
    public List<FilingDto> getFilingHistory(
            @PathVariable Long assetId
    ) {
        IpAsset asset = ipAssetRepository.findById(assetId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Asset not found for id=" + assetId
                        )
                );

        return filingRepository.findByIpAsset(asset)
                .stream()
                .map(this::mapToDto)
                .toList();
    }
    
    @GetMapping("/lifecycle/{assetId}")
    public ResponseEntity<List<Map<String, Object>>> getAssetLifecycle(
            @PathVariable Long assetId,
            Principal principal
    ) {
        User user = userRepo.findByEmail(principal.getName()).orElseThrow();

        IpAsset asset = ipAssetRepository.findById(assetId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        // Security: ensure user owns subscription
        subscriptionRepository.findByUserAndIpAsset(user, asset)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN));

        List<Map<String, Object>> lifecycle = new ArrayList<>();

        if (asset.getApplicationDate() != null) {
            lifecycle.add(Map.of(
                "stage", "APPLICATION",
                "date", asset.getApplicationDate()
            ));
        }


        if (asset.getGrantDate() != null) {
            lifecycle.add(Map.of(
                "stage", "GRANTED",
                "date", asset.getGrantDate()
            ));
        }

        if ("RENEWAL".equals(asset.getLegalStatus())) {
            lifecycle.add(Map.of(
                "stage", "RENEWAL",
                "date", asset.getUpdatedAt()
            ));
        }

        if (asset.getExpiryDate() != null) {
            lifecycle.add(Map.of(
                "stage", "EXPIRED",
                "date", asset.getExpiryDate()
            ));
        }

        return ResponseEntity.ok(lifecycle);
    }




    private FilingDto mapToDto(Filing filing) {
        FilingDto dto = new FilingDto();

        dto.setId(filing.getId());
        dto.setStatus(filing.getStatus());
        dto.setDescription(filing.getDescription());

        // LocalDateTime → LocalDate
        if (filing.getDate() != null) {
            dto.setDate(filing.getDate().toLocalDate());
        }

        return dto;
    }
    
    @GetMapping("/analyst/lifecycle")
    public ResponseEntity<Map<String, Long>> getLifecycleStats(Principal principal) {

        User user = userRepo.findByEmail(principal.getName()).orElseThrow();
        List<Subscription> subs = subscriptionRepository.findByUser(user);

        // 🔥 IMPORTANT: lifecycle calculate + set
        subs.forEach(s -> {
            IpAsset asset = s.getIpAsset();
            String lifecycle = filingtrackerservices.calculateLifecycle(asset);
            asset.setLegalStatus(lifecycle);
        });

        Map<String, Long> stats = new HashMap<>();
        stats.put("APPLICATION", subs.stream()
            .filter(s -> "APPLICATION".equals(s.getIpAsset().getLegalStatus())).count());
        stats.put("GRANTED", subs.stream()
            .filter(s -> "GRANTED".equals(s.getIpAsset().getLegalStatus())).count());
        stats.put("RENEWAL", subs.stream()
            .filter(s -> "RENEWAL".equals(s.getIpAsset().getLegalStatus())).count());
        stats.put("EXPIRED", subs.stream()
            .filter(s -> "EXPIRED".equals(s.getIpAsset().getLegalStatus())).count());

        return ResponseEntity.ok(stats);
    }


    
    
    

    // Analyst Dashboard Widgets ke liye stats [cite: 5, 12]
    @GetMapping("/analyst/stats")
    public ResponseEntity<Map<String, Long>> getAnalystStats(Principal principal) {
        User user = userRepo.findByEmail(principal.getName()).get();
        List<Subscription> subs = subscriptionRepository.findByUser(user);

        long granted = subs.stream().filter(s -> "Granted".equals(s.getIpAsset().getStatus())).count();
        long pending = subs.stream().filter(s -> "Pending".equals(s.getIpAsset().getStatus())).count();

        Map<String, Long> stats = new HashMap<>();
        stats.put("granted", granted);
        stats.put("pending", pending);
        return ResponseEntity.ok(stats);
    }
    
    
}





    
    
    
    
    
