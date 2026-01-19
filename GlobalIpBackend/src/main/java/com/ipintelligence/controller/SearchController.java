package com.ipintelligence.controller;

import com.ipintelligence.dto.IpAssetDto;
import com.ipintelligence.dto.SearchRequestDto;
import com.ipintelligence.dto.SearchResultDto;
import com.ipintelligence.model.IpAsset;
import com.ipintelligence.model.SearchHistory;
import com.ipintelligence.model.User;
import com.ipintelligence.service.IpSearchService;
import com.ipintelligence.service.impl.EpoApiClient;
import com.ipintelligence.service.impl.TmViewSeleniumService;
import com.ipintelligence.repo.UserRepository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/api/search")
public class SearchController {

    private final IpSearchService ipSearchService;
    private final EpoApiClient epoApiClient;
    private final TmViewSeleniumService tmViewSeleniumService;
    private final UserRepository userRepository;

    public SearchController(IpSearchService ipSearchService, EpoApiClient epoApiClient, TmViewSeleniumService tmViewSeleniumService, UserRepository userRepository) {
        this.ipSearchService = ipSearchService;
        this.epoApiClient = epoApiClient;
        this.tmViewSeleniumService = tmViewSeleniumService;
        this.userRepository = userRepository;
    }
    

    // Direct endpoint for Selenium-based TMView search (for testing/fallback)
    @PostMapping("/trademark/selenium")
    public ResponseEntity<SearchResultDto> searchTrademarkSelenium(@RequestBody Map<String, String> body) {
        String query = body.getOrDefault("query", "");
        List<IpAssetDto> dtos = tmViewSeleniumService.searchTrademark(query);
        SearchResultDto result = new SearchResultDto();
        result.setAssets(dtos);
        result.setTotalElements(dtos.size());
        result.setDataSource("TMVIEW-SELENIUM");
        return ResponseEntity.ok(result);
    }
    

    @PostMapping("/all")
    public ResponseEntity<SearchResultDto> searchAllSources(
            @RequestBody SearchRequestDto searchRequest,
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User principal) {

        log.info("[SEARCH-DEBUG] Incoming search request: assetType={}, dataSources={}, patentOffice={}, fromDate={}, toDate={}, inventor={}, assignee={}, query={}",
                searchRequest.getAssetType(),
                searchRequest.getDataSources(),
                searchRequest.getPatentOffice(),
                searchRequest.getFromDate(),
                searchRequest.getToDate(),
                searchRequest.getInventor(),
                searchRequest.getAssignee(),
                searchRequest.getQuery());

        // Always fetch the real User entity from DB by email (or username)
        User realUser = null;
        if (principal != null && principal.getUsername() != null) {
            realUser = userRepository.findByEmail(principal.getUsername()).orElse(null);
        }
        SearchResultDto result = ipSearchService.searchAcrossAllSources(searchRequest, realUser);
        // Save search history for dashboard counts
        ipSearchService.saveSearchHistory(searchRequest, result, realUser);
        return ResponseEntity.ok(result);
    }
    
    
    @PostMapping("/filters/{jurisdiction}")
    public ResponseEntity<SearchResultDto> searchByJurisdiction(
            @PathVariable String jurisdiction,
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User principal) {

        SearchRequestDto searchRequest = new SearchRequestDto();

        // ✅ String expected, String passed
        searchRequest.setPatentOffice(jurisdiction);

        log.info("[SEARCH] Jurisdiction filter: {}", jurisdiction);

        User realUser = null;
        if (principal != null && principal.getUsername() != null) {
            realUser = userRepository
                    .findByEmail(principal.getUsername())
                    .orElse(null);
        }

        SearchResultDto result =
                ipSearchService.searchAcrossAllSources(searchRequest, realUser);

        return ResponseEntity.ok(result);
    }


    
    

    @PostMapping("/source/{dataSource}")
    public ResponseEntity<SearchResultDto> searchSpecificSource(
            @PathVariable String dataSource,
            @RequestBody SearchRequestDto searchRequest,
            @AuthenticationPrincipal User currentUser) {

        log.info("User: {}, Searching {} for query: {}",
                currentUser != null ? currentUser.getEmail() : "anonymous",
                dataSource,
                searchRequest.getQuery());

        SearchResultDto result = ipSearchService.searchSpecificSource(searchRequest, dataSource, currentUser);
        // Save search history for dashboard counts
        ipSearchService.saveSearchHistory(searchRequest, result, currentUser);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/local")
    public ResponseEntity<Page<IpAsset>> searchLocalDatabase(@RequestBody SearchRequestDto searchRequest) {
        Pageable pageable = PageRequest.of(
                searchRequest.getPage(),
                searchRequest.getSize(),
                Sort.Direction.fromString(searchRequest.getSortDirection()),
                searchRequest.getSortBy()
        );

        Page<IpAsset> result = ipSearchService.searchLocalDatabase(searchRequest, pageable);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/asset/{assetId}")
    public ResponseEntity<IpAssetDto> getAssetDetails(@PathVariable Long assetId) {
        IpAssetDto asset = ipSearchService.getAssetDetails(assetId);
        if (asset != null) {
            return ResponseEntity.ok(asset);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/asset/{dataSource}/{externalId}")
    public ResponseEntity<IpAssetDto> getAssetByExternalId(
            @PathVariable String dataSource,
            @PathVariable String externalId) {

        IpAssetDto asset = ipSearchService.getAssetDetailsByExternalId(externalId, dataSource);
        if (asset != null) {
            return ResponseEntity.ok(asset);
        }
        return ResponseEntity.notFound().build();
    }

    // ✅ NEW ENDPOINT: Fetch full patent details from EPO directly
    @GetMapping("/patent/{externalId}")
    public ResponseEntity<IpAssetDto> getPatentDetailsFromSource(
            @PathVariable String externalId,
            @RequestParam(required = false, defaultValue = "EPO") String source) {

        log.info("Fetching full patent details for: {} from {}", externalId, source);

        if ("EPO".equalsIgnoreCase(source)) {
            try {
                IpAssetDto details = epoApiClient.getAssetDetails(externalId);
                if (details != null) {
                    log.info("Successfully fetched details for patent: {}", externalId);
                    return ResponseEntity.ok(details);
                }
            } catch (Exception e) {
                log.error("Error fetching patent details for {}: {}", externalId, e.getMessage());
            }
        }

        log.warn("Patent not found: {} from {}", externalId, source);
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/history")
    public ResponseEntity<List<SearchHistory>> getUserSearchHistory(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        if (currentUser == null) {
            return ResponseEntity.badRequest().build();
        }

        Pageable pageable = PageRequest.of(page, size);
        List<SearchHistory> history = ipSearchService.getUserSearchHistory(currentUser, pageable);
        return ResponseEntity.ok(history);
    }
    
    
    // Fills the "Jurisdiction" dropdown in your UI
    @GetMapping("/filters/jurisdictions")
    public List<String> getJurisdictions() {
        return List.of("US", "EP", "WO", "JP", "CN", "KR", "DE", "GB", "FR");
    }

    // Fills the data sources list
    @GetMapping("/filters/data-sources")
    public List<String> getDataSources() {
        return List.of("Google Patents Public Data", "Internal BigQuery DB");
    }
    

//    @GetMapping("/filters/jurisdictions")
//    public ResponseEntity<List<String>> getAvailableJurisdictions() {
//        List<String> jurisdictions = ipSearchService.getAvailableJurisdictions();
//        return ResponseEntity.ok(jurisdictions);
//    }

    @GetMapping("/filters/patent-offices")
    public ResponseEntity<List<String>> getAvailablePatentOffices() {
        List<String> patentOffices = ipSearchService.getAvailablePatentOffices();
        return ResponseEntity.ok(patentOffices);
    }

//    @GetMapping("/filters/data-sources")
//    public ResponseEntity<List<String>> getAvailableDataSources() {
//        List<String> dataSources = ipSearchService.getAvailableDataSources();
//        return ResponseEntity.ok(dataSources);
//    }

    @GetMapping("/filters/asset-types")
    public ResponseEntity<IpAsset.AssetType[]> getAvailableAssetTypes() {
        return ResponseEntity.ok(IpAsset.AssetType.values());
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getSearchStats(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(Map.of(
                "message", "Search statistics endpoint - to be implemented",
                "totalSearches", 0,
                "recentSearches", 0
        ));
    }
}
