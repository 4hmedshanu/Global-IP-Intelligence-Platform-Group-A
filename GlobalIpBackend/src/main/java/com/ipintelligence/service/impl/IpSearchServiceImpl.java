package com.ipintelligence.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ipintelligence.dto.IpAssetDto;
import com.ipintelligence.dto.SearchRequestDto;
import com.ipintelligence.dto.SearchResultDto;
import com.ipintelligence.model.IpAsset;
import com.ipintelligence.model.SearchHistory;
import com.ipintelligence.model.User;
import com.ipintelligence.repo.IpAssetRepository;
import com.ipintelligence.repo.SearchHistoryRepository;
import com.ipintelligence.service.IpSearchService;
import com.ipintelligence.service.api.PatentOfficeApiClient;
import com.ipintelligence.service.GooglePatentService;
import com.ipintelligence.service.impl.TmViewSeleniumService;
import org.springframework.beans.factory.annotation.Autowired;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@Slf4j
public class IpSearchServiceImpl implements IpSearchService {

    @Override
    public boolean isAvailable() {
        // Optionally, check DB or a simple query
        try {
            ipAssetRepository.count(); // simple DB check
            return true;
        } catch (Exception e) {
            log.error("IpSearchService health check failed", e);
            return false;
        }
    }

    private final List<PatentOfficeApiClient> apiClients;
    private final IpAssetRepository ipAssetRepository;
    private final SearchHistoryRepository searchHistoryRepository;
    private final ObjectMapper objectMapper;
    private final GooglePatentService googlePatentService;
    private final TmViewSeleniumService tmViewSeleniumService;

    @Autowired
    public IpSearchServiceImpl(List<PatentOfficeApiClient> apiClients,
                               IpAssetRepository ipAssetRepository,
                               SearchHistoryRepository searchHistoryRepository,
                               ObjectMapper objectMapper,
                               GooglePatentService googlePatentService,
                               TmViewSeleniumService tmViewSeleniumService) {
        this.apiClients = apiClients;
        this.ipAssetRepository = ipAssetRepository;
        this.searchHistoryRepository = searchHistoryRepository;
        this.objectMapper = objectMapper;
        this.googlePatentService = googlePatentService;
        this.tmViewSeleniumService = tmViewSeleniumService;
    }

    @Override
    @Transactional
    public SearchResultDto searchAcrossAllSources(SearchRequestDto searchRequest, User user) {

        log.info("User: {}, Searching all sources for query: {}, searchType: {}",
                user != null ? user.getEmail() : "null",
                searchRequest.getQuery(),
                searchRequest.getSearchType());

        try {
            List<CompletableFuture<SearchResultDto>> futures = new ArrayList<>();

            // Determine if this is a trademark or patent search
            boolean isTrademark = false;
            if (searchRequest.getAssetType() != null && searchRequest.getAssetType().toString().equalsIgnoreCase("TRADEMARK")) {
                isTrademark = true;
            } else if (searchRequest.getDataSources() != null && searchRequest.getDataSources().stream().anyMatch(ds -> ds.equalsIgnoreCase("TMVIEW"))) {
                isTrademark = true;
            } else if (searchRequest.getPatentOffice() != null && searchRequest.getPatentOffice().equalsIgnoreCase("TMVIEW")) {
                isTrademark = true;
            }

            if (isTrademark) {
                // For trademarks, use Selenium-based TMView search as fallback
                searchRequest.setInventor(null);
                futures.add(CompletableFuture.supplyAsync(() -> {
                    try {
                        List<IpAssetDto> dtos = tmViewSeleniumService.searchTrademark(
                                searchRequest.getQuery() != null ? searchRequest.getQuery()
                                        : (searchRequest.getAssignee() != null ? searchRequest.getAssignee() : "")
                        );
                        SearchResultDto result = new SearchResultDto();
                        result.setAssets(dtos);
                        result.setTotalElements(dtos.size());
                        result.setDataSource("TMVIEW-SELENIUM");
                        return result;
                    } catch (Exception e) {
                        log.error("Error searching with TMView Selenium fallback", e);
                        return createEmptyResult(searchRequest, "TMVIEW-SELENIUM");
                    }
                }));
            } else {
                // For patents, call all patent sources
                apiClients.stream()
                        .filter(client -> !client.getDataSource().equalsIgnoreCase("TMVIEW") && client.isAvailable())
                        .forEach(client
                                        -> futures.add(CompletableFuture.supplyAsync(() -> {
                                    try {
                                        return client.search(searchRequest);
                                    } catch (Exception e) {
                                        log.error("Error searching with client: {}", client.getDataSource(), e);
                                        return createEmptyResult(searchRequest, client.getDataSource());
                                    }
                                }))
                        );
                // Always include GooglePatentService as well
                futures.add(CompletableFuture.supplyAsync(() -> {
                    try {
                        return googlePatentService.search(searchRequest);
                    } catch (Exception e) {
                        log.error("Error searching Google Patents (BigQuery): {}", e.getMessage(), e);
                        return createEmptyResult(searchRequest, "GOOGLE_PATENT");
                    }
                }));
            }

            // Combine results from all sources
            SearchResultDto combinedResult = combineSearchResults(futures, searchRequest);

            // Post-process: filter combined results by assignee, inventor, and date
            List<IpAssetDto> filteredAssets = combinedResult.getAssets();
            if (filteredAssets == null) {
                filteredAssets = new ArrayList<>();
            }
            if (searchRequest.getAssignee() != null && !searchRequest.getAssignee().isEmpty()) {
                String assigneeFilter = searchRequest.getAssignee().toLowerCase();
                filteredAssets = filteredAssets.stream()
                        .filter(a -> a.getAssignee() != null && a.getAssignee().toLowerCase().contains(assigneeFilter))
                        .collect(Collectors.toList());
            }
            if (searchRequest.getInventor() != null && !searchRequest.getInventor().isEmpty()) {
                String inventorFilter = searchRequest.getInventor().toLowerCase();
                filteredAssets = filteredAssets.stream()
                        .filter(a -> a.getInventor() != null && a.getInventor().toLowerCase().contains(inventorFilter))
                        .collect(Collectors.toList());
            }
            if (searchRequest.getFromDate() != null) {
                filteredAssets = filteredAssets.stream()
                        .filter(a -> a.getPublicationDate() != null && !a.getPublicationDate().isBefore(searchRequest.getFromDate()))
                        .collect(Collectors.toList());
            }
            if (searchRequest.getToDate() != null) {
                filteredAssets = filteredAssets.stream()
                        .filter(a -> a.getPublicationDate() != null && !a.getPublicationDate().isAfter(searchRequest.getToDate()))
                        .collect(Collectors.toList());
            }
            combinedResult.setAssets(filteredAssets);
            combinedResult.setTotalElements(filteredAssets.size());

            // ✅ REMOVED: Don't save history here - controller will handle it

            // Save new assets to database
            if (combinedResult.getAssets() != null && !combinedResult.getAssets().isEmpty()) {
                saveSearchResults(combinedResult.getAssets());
            }

            return combinedResult;

        } catch (Exception e) {
            log.error("Error during search across all sources: ", e);
            throw new RuntimeException("Search failed: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public SearchResultDto searchSpecificSource(SearchRequestDto searchRequest, String dataSource, User user) {
        log.info("User: {}, Searching {} for query: {}",
                user != null ? user.getEmail() : "null",
                dataSource,
                searchRequest.getQuery());

        try {
            SearchResultDto result;

            // Check if it's Google Patents
            if ("GOOGLE_PATENT".equals(dataSource)) {
                result = googlePatentService.search(searchRequest);
            } else {
                PatentOfficeApiClient client = apiClients.stream()
                        .filter(c -> c.getDataSource().equals(dataSource))
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("Data source not found: " + dataSource));

                if (!client.isAvailable()) {
                    throw new RuntimeException("Data source is not available: " + dataSource);
                }

                result = client.search(searchRequest);
            }

            // ✅ REMOVED: Don't save history here - controller will handle it

            // Save new assets to database
            if (!result.getAssets().isEmpty()) {
                saveSearchResults(result.getAssets());
            }

            return result;

        } catch (Exception e) {
            log.error("Error during search for specific source {}: ", dataSource, e);
            throw new RuntimeException("Search failed for " + dataSource + ": " + e.getMessage(), e);
        }
    }

    @Override
    public Page<IpAsset> searchLocalDatabase(SearchRequestDto searchRequest, Pageable pageable) {
        if (searchRequest.getQuery() != null && !searchRequest.getQuery().isEmpty()) {
            return ipAssetRepository.searchByKeyword(searchRequest.getQuery(), pageable);
        }

        return ipAssetRepository.searchWithFilters(
                searchRequest.getTitle(),
                searchRequest.getInventor(),
                searchRequest.getAssignee(),
                searchRequest.getJurisdiction(),
                searchRequest.getAssetType(),
                searchRequest.getPatentOffice(),
                searchRequest.getFromDate(),
                searchRequest.getToDate(),
                pageable
        );
    }

    @Override
    public IpAssetDto getAssetDetails(Long assetId) {
        Optional<IpAsset> asset = ipAssetRepository.findById(assetId);
        return asset.map(this::convertToDto).orElse(null);
    }

    @Override
    public IpAssetDto getAssetDetailsByExternalId(String externalId, String dataSource) {
        // First try to find in local database
        Optional<IpAsset> localAsset = ipAssetRepository.findByExternalIdAndPatentOffice(externalId, dataSource);
        if (localAsset.isPresent()) {
            return convertToDto(localAsset.get());
        }

        // If not found locally, try to fetch from API
        if ("GOOGLE_PATENT".equals(dataSource)) {
            IpAssetDto assetDto = googlePatentService.getAssetDetails(externalId);
            if (assetDto != null) {
                saveSearchResults(Collections.singletonList(assetDto));
                return assetDto;
            }
        } else {
            PatentOfficeApiClient client = apiClients.stream()
                    .filter(c -> c.getDataSource().equals(dataSource))
                    .findFirst()
                    .orElse(null);

            if (client != null && client.isAvailable()) {
                IpAssetDto assetDto = client.getAssetDetails(externalId);
                if (assetDto != null) {
                    saveSearchResults(Collections.singletonList(assetDto));
                    return assetDto;
                }
            }
        }

        return null;
    }

    @Override
    @Transactional
    public List<IpAssetDto> saveSearchResults(List<IpAssetDto> assets) {
        List<IpAsset> savedAssets = new ArrayList<>();

        for (IpAssetDto assetDto : assets) {
            // Check if asset already exists
            Optional<IpAsset> existing = ipAssetRepository.findByExternalIdAndPatentOffice(
                    assetDto.getExternalId(), assetDto.getPatentOffice());

            IpAsset asset;
            if (existing.isPresent()) {
                asset = existing.get();
                // Update existing asset with new data
                updateAssetFromDto(asset, assetDto);
            } else {
                asset = convertToEntity(assetDto);
            }

            savedAssets.add(ipAssetRepository.save(asset));
        }

        return savedAssets.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    @Override
    public List<SearchHistory> getUserSearchHistory(User user, Pageable pageable) {
        Page<SearchHistory> historyPage = searchHistoryRepository.findByUserOrderByCreatedAtDesc(user, pageable);
        return historyPage.getContent();
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveSearchHistory(SearchRequestDto searchRequest, SearchResultDto result, User user) {
        log.info("[SEARCH-DEBUG] Saving search history for user: {}",
                user != null ? (user.getId() + ", " + user.getEmail() + ", username=" + user.getUsername()) : "null");
        try {
            // Validate user is not null
            if (user == null) {
                log.warn("[SEARCH-DEBUG] Cannot save search history: user is null");
                return;
            }

            // Validate user has an ID (is persisted)
            if (user.getId() == null) {
                log.warn("[SEARCH-DEBUG] Cannot save search history: user ID is null. User: {}", user);
                return;
            }

            SearchHistory history = new SearchHistory();
            history.setUser(user);

            // Always set search_query to the first non-null of query, inventor, or assignee
            String searchQuery = searchRequest.getQuery();
            if (searchQuery == null || searchQuery.trim().isEmpty()) {
                searchQuery = searchRequest.getInventor();
            }
            if (searchQuery == null || searchQuery.trim().isEmpty()) {
                searchQuery = searchRequest.getAssignee();
            }
            if (searchQuery == null) {
                searchQuery = "";
            }
            history.setSearchQuery(searchQuery);

            // Safely serialize search filters
            try {
                history.setSearchFilters(objectMapper.writeValueAsString(searchRequest));
            } catch (Exception e) {
                log.warn("Failed to serialize search filters, using query only", e);
                history.setSearchFilters(searchQuery);
            }

            history.setResultsCount((int) result.getTotalElements());

            // Normalize dataSource for dashboard counting
            String ds = result.getDataSource();
            if (ds != null && ds.contains(",")) {
                ds = ds.split(",")[0].trim();
            }
            if (ds != null && ds.toUpperCase().startsWith("TMVIEW")) {
                ds = "TMVIEW";
            }
            if (ds != null && ds.toUpperCase().startsWith("USPTO")) {
                ds = "USPTO";
            }
            history.setDataSource(ds);
            history.setSearchType(determineSearchType(searchRequest));

            searchHistoryRepository.save(history);
            log.info("[SEARCH-DEBUG] Search history saved successfully for user: id={}, email={}, username={}",
                    user.getId(), user.getEmail(), user.getUsername());

        } catch (Exception e) {
            log.error("Error saving search history for user {}: {}",
                    user != null ? user.getEmail() : "null",
                    e.getMessage(), e);
        }
    }

    @Override
    public List<String> getAvailableJurisdictions() {
        return ipAssetRepository.findDistinctJurisdictions();
    }

    @Override
    public List<String> getAvailablePatentOffices() {
        return ipAssetRepository.findDistinctPatentOffices();
    }

    @Override
    public List<String> getAvailableDataSources() {
        List<String> dataSources = new ArrayList<>();
        dataSources.add("GOOGLE_PATENT");
        dataSources.addAll(apiClients.stream()
                .map(PatentOfficeApiClient::getDataSource)
                .collect(Collectors.toList()));
        return dataSources;
    }

    private SearchResultDto combineSearchResults(List<CompletableFuture<SearchResultDto>> futures, SearchRequestDto searchRequest) {
        SearchResultDto combinedResult = new SearchResultDto();
        List<IpAssetDto> allAssets = new ArrayList<>();
        StringBuilder dataSources = new StringBuilder();

        for (CompletableFuture<SearchResultDto> future : futures) {
            try {
                SearchResultDto result = future.get();
                if (result.getAssets() != null) {
                    allAssets.addAll(result.getAssets());
                }
                if (dataSources.length() > 0) {
                    dataSources.append(", ");
                }
                dataSources.append(result.getDataSource());
            } catch (Exception e) {
                log.error("Error getting search result from future", e);
            }
        }

        // Remove duplicates based on externalId and patentOffice
        Map<String, IpAssetDto> uniqueAssets = new HashMap<>();
        for (IpAssetDto asset : allAssets) {
            String key = (asset.getExternalId() != null ? asset.getExternalId() : UUID.randomUUID().toString())
                    + "_"
                    + (asset.getPatentOffice() != null ? asset.getPatentOffice() : "EPO");
            uniqueAssets.put(key, asset);
        }

        int uniqueCount = uniqueAssets.size();
        List<IpAssetDto> finalAssets = new ArrayList<>(uniqueAssets.values());
        log.info("[AGG-DEBUG] Returning {} unique assets. Sample: {}", uniqueCount, finalAssets.stream().limit(3).toList());

        combinedResult.setAssets(finalAssets);
        combinedResult.setTotalElements(uniqueCount);
        combinedResult.setCurrentPage(searchRequest.getPage());
        combinedResult.setPageSize(searchRequest.getSize());
        combinedResult.setTotalPages((int) Math.ceil((double) uniqueCount / searchRequest.getSize()));
        combinedResult.setHasNext(searchRequest.getPage() < combinedResult.getTotalPages() - 1);
        combinedResult.setHasPrevious(searchRequest.getPage() > 0);
        combinedResult.setSearchQuery(searchRequest.getQuery());
        combinedResult.setDataSource(dataSources.toString());

        return combinedResult;
    }

    private SearchResultDto createEmptyResult(SearchRequestDto searchRequest, String dataSource) {
        SearchResultDto result = new SearchResultDto();
        result.setDataSource(dataSource);
        result.setSearchQuery(searchRequest.getQuery());
        result.setCurrentPage(searchRequest.getPage());
        result.setPageSize(searchRequest.getSize());
        result.setAssets(new ArrayList<>());
        result.setTotalElements(0);
        result.setTotalPages(0);
        result.setHasNext(false);
        result.setHasPrevious(false);
        return result;
    }

    private SearchHistory.SearchType determineSearchType(SearchRequestDto searchRequest) {
        if (searchRequest.getInventor() != null && !searchRequest.getInventor().isEmpty()) {
            return SearchHistory.SearchType.INVENTOR;
        }
        if (searchRequest.getAssignee() != null && !searchRequest.getAssignee().isEmpty()) {
            return SearchHistory.SearchType.ASSIGNEE;
        }
        if (searchRequest.getClassification() != null && !searchRequest.getClassification().isEmpty()) {
            return SearchHistory.SearchType.CLASSIFICATION;
        }
        if (hasMultipleFilters(searchRequest)) {
            return SearchHistory.SearchType.ADVANCED;
        }
        return SearchHistory.SearchType.KEYWORD;
    }

    private boolean hasMultipleFilters(SearchRequestDto searchRequest) {
        int filterCount = 0;
        if (searchRequest.getQuery() != null && !searchRequest.getQuery().isEmpty()) {
            filterCount++;
        }
        if (searchRequest.getTitle() != null && !searchRequest.getTitle().isEmpty()) {
            filterCount++;
        }
        if (searchRequest.getInventor() != null && !searchRequest.getInventor().isEmpty()) {
            filterCount++;
        }
        if (searchRequest.getAssignee() != null && !searchRequest.getAssignee().isEmpty()) {
            filterCount++;
        }
        if (searchRequest.getJurisdiction() != null) {
            filterCount++;
        }
        if (searchRequest.getAssetType() != null) {
            filterCount++;
        }
        if (searchRequest.getFromDate() != null || searchRequest.getToDate() != null) {
            filterCount++;
        }
        return filterCount > 2;
    }

    private IpAssetDto convertToDto(IpAsset asset) {
        IpAssetDto dto = new IpAssetDto();
        BeanUtils.copyProperties(asset, dto);
        return dto;
    }

    private IpAsset convertToEntity(IpAssetDto dto) {
        IpAsset asset = new IpAsset();
        BeanUtils.copyProperties(dto, asset);
        return asset;
    }

    private void updateAssetFromDto(IpAsset asset, IpAssetDto dto) {
        asset.setTitle(dto.getTitle());
        asset.setDescription(dto.getDescription());
        asset.setStatus(dto.getStatus());
        asset.setInventor(dto.getInventor());
        asset.setAssignee(dto.getAssignee());
        asset.setKeywords(dto.getKeywords());
        asset.setLegalStatus(dto.getLegalStatus());
    }
}
