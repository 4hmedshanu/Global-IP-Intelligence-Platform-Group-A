package com.ipintelligence.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ipintelligence.dto.IpAssetDto;
import com.ipintelligence.dto.SearchRequestDto;
import com.ipintelligence.dto.SearchResultDto;
import com.ipintelligence.service.api.PatentOfficeApiClient;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;

@Component
@Slf4j
public class GooglePatentApiClient implements PatentOfficeApiClient {

    private final ObjectMapper objectMapper;

    @Value("${google.cse.api-key:}")
    private String apiKey;

    @Value("${google.cse.cx:}")
    private String cx;

    private final RestTemplate restTemplate = new RestTemplate();

    public GooglePatentApiClient(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    // No credentials needed for Custom Search API
    @Override
    public String getDataSource() {
        return "GOOGLE_PATENT";
    }

    @Override
    public SearchResultDto search(SearchRequestDto searchRequest) {
        log.info("Searching Google Patents via Custom Search API for query: {}", searchRequest.getQuery());
        SearchResultDto result = new SearchResultDto();
        result.setAssets(new java.util.ArrayList<>());
        result.setCurrentPage(searchRequest != null ? searchRequest.getPage() : 0);
        result.setPageSize(searchRequest != null ? searchRequest.getSize() : 20);
        result.setSearchQuery(searchRequest != null ? searchRequest.getQuery() : null);
        result.setDataSource("GOOGLE_PATENT");

        if (apiKey == null || apiKey.isEmpty() || cx == null || cx.isEmpty() || searchRequest == null || searchRequest.getQuery() == null) {
            log.warn("Google CSE API key, cx, or query missing");
            result.setTotalElements(0L);
            result.setTotalPages(0);
            result.setHasNext(false);
            result.setHasPrevious(false);
            return result;
        }

        String url = String.format(
                "https://www.googleapis.com/customsearch/v1?q=%s&key=%s&cx=%s&siteSearch=patents.google.com",
                searchRequest.getQuery(), apiKey, cx
        );
        try {
            String response = restTemplate.getForObject(url, String.class);
            log.info("Google CSE response: {}", response);
            // Parse response JSON and map to IpAssetDto list
            com.fasterxml.jackson.databind.JsonNode root = objectMapper.readTree(response);
            java.util.List<IpAssetDto> assets = new java.util.ArrayList<>();
            if (root.has("items")) {
                for (com.fasterxml.jackson.databind.JsonNode item : root.get("items")) {
                    IpAssetDto asset = new IpAssetDto();
                    // Title
                    asset.setTitle(item.has("title") ? item.get("title").asText() : null);
                    // Description (snippet)
                    asset.setDescription(item.has("snippet") ? item.get("snippet").asText() : null);
                    // Link (use as externalId)
                    asset.setExternalId(item.has("link") ? item.get("link").asText() : null);
                    // Set assetType to PATENT
                    asset.setAssetType(com.ipintelligence.model.IpAsset.AssetType.PATENT);
                    // Try to extract publication/application numbers from title or snippet if possible
                    String title = asset.getTitle() != null ? asset.getTitle() : "";
                    String snippet = asset.getDescription() != null ? asset.getDescription() : "";
                    // Simple regex for publication number (e.g., US1234567B2)
                    java.util.regex.Matcher pubMatcher = java.util.regex.Pattern.compile("([A-Z]{2}\\d{7,}[A-Z0-9]*)").matcher(title + " " + snippet);
                    if (pubMatcher.find()) {
                        asset.setPublicationNumber(pubMatcher.group(1));
                    }
                    // Set patentOffice if possible (e.g., US, EP, WO)
                    if (asset.getPublicationNumber() != null && asset.getPublicationNumber().length() >= 2) {
                        asset.setPatentOffice(asset.getPublicationNumber().substring(0, 2));
                    } else {
                        asset.setPatentOffice("GOOGLE_PATENT");
                    }
                    // Set jurisdiction as patentOffice for now
                    asset.setJurisdiction(asset.getPatentOffice());
                    // Set keywords as search query
                    asset.setKeywords(searchRequest.getQuery());
                    // Set status as unknown (Google CSE does not provide)
                    asset.setStatus("UNKNOWN");
                    // Set inventor, assignee, classifications, legalStatus, dates as null (not available)
                    // Set createdAt/updatedAt to now
                    asset.setCreatedAt(java.time.LocalDateTime.now());
                    asset.setUpdatedAt(java.time.LocalDateTime.now());
                    assets.add(asset);
                }
            }
            result.setAssets(assets);
            result.setTotalElements(assets.size());
            result.setTotalPages(1); // CSE free tier returns up to 10 results per page
            result.setHasNext(false); // Pagination not implemented
            result.setHasPrevious(false);
        } catch (Exception e) {
            log.error("Error calling or parsing Google Custom Search API", e);
            result.setTotalElements(0L);
            result.setTotalPages(0);
            result.setHasNext(false);
            result.setHasPrevious(false);
        }
        return result;
    }

    @Override
    public IpAssetDto getAssetDetails(String externalId) {
        // TODO: Implement asset details fetch
        return null;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public int getRateLimitPerMinute() {
        return 60;
    }
}
