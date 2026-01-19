package com.ipintelligence.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ipintelligence.dto.IpAssetDto;
import com.ipintelligence.dto.SearchRequestDto;
import com.ipintelligence.dto.SearchResultDto;
import com.ipintelligence.model.IpAsset;
import com.ipintelligence.service.api.PatentOfficeApiClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class TmViewApiClient implements PatentOfficeApiClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.api.tmview.base-url:https://www.tmdn.org/tmview/api}")
    private String baseUrl;

    @Value("${app.api.tmview.api-key:}")
    private String apiKey;

    public TmViewApiClient(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public String getDataSource() {
        return "TMVIEW";
    }

    @Override
    public SearchResultDto search(SearchRequestDto searchRequest) {
        try {
            String url = buildSearchUrl(searchRequest);
            HttpHeaders headers = createHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            log.info("[TMVIEW] Request URL: {}", url);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            SearchResultDto result = parseSearchResponse(response.getBody(), searchRequest);
            if (result.getAssets() != null && !result.getAssets().isEmpty()) {
                log.info("[TMVIEW] First 3 results:");
                result.getAssets().stream().limit(3).forEach(a
                        -> log.info("[TMVIEW] externalId={}, title={}, applicationDate={}, grantDate={}",
                                a.getExternalId(), a.getTitle(), a.getApplicationDate(), a.getGrantDate())
                );
            } else {
                log.info("[TMVIEW] No results returned.");
            }
            return result;
        } catch (Exception e) {
            log.error("Error searching TMView API", e);
            return createEmptyResult(searchRequest);
        }
    }

    @Override
    public IpAssetDto getAssetDetails(String externalId) {
        try {
            String url = baseUrl + "/trademark/" + externalId;
            HttpHeaders headers = createHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            return parseAssetDetails(response.getBody());
        } catch (Exception e) {
            log.error("Error fetching TMView asset details for ID: {}", externalId, e);
            return null;
        }
    }

    @Override
    public boolean isAvailable() {
        try {
            HttpHeaders headers = createHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    baseUrl + "/health",
                    HttpMethod.GET,
                    entity,
                    String.class
            );
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.warn("TMView API is not available", e);
            return false;
        }
    }

    @Override
    public int getRateLimitPerMinute() {
        return 60;
    }

    private String buildSearchUrl(SearchRequestDto request) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(baseUrl + "/search")
                .queryParam("start", request.getPage() * request.getSize())
                .queryParam("rows", request.getSize());

        if (request.getQuery() != null && !request.getQuery().isEmpty()) {
            builder.queryParam("q", request.getQuery());
        }

        if (request.getAssignee() != null && !request.getAssignee().isEmpty()) {
            builder.queryParam("owner", request.getAssignee());
        }

        if (request.getJurisdiction() != null && !request.getJurisdiction().isEmpty()) {
            builder.queryParam("office", request.getJurisdiction());
        }

        if (request.getFromDate() != null) {
            builder.queryParam("dateFrom", request.getFromDate().format(DateTimeFormatter.ISO_LOCAL_DATE));
        }

        if (request.getToDate() != null) {
            builder.queryParam("dateTo", request.getToDate().format(DateTimeFormatter.ISO_LOCAL_DATE));
        }

        return builder.toUriString();
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Accept", "application/json");

        if (apiKey != null && !apiKey.isEmpty()) {
            headers.set("X-API-Key", apiKey);
        }

        return headers;
    }

    private SearchResultDto parseSearchResponse(String responseBody, SearchRequestDto request) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode results = root.path("trademarks");

            List<IpAssetDto> assets = new ArrayList<>();

            if (results.isArray()) {
                for (JsonNode node : results) {
                    IpAssetDto asset = new IpAssetDto();
                    asset.setExternalId(node.path("applicationNumber").asText());
                    asset.setTitle(node.path("markVerbatim").asText());
                    asset.setDescription(node.path("goodsServices").asText());
                    asset.setAssetType(IpAsset.AssetType.TRADEMARK);
                    asset.setApplicationNumber(node.path("applicationNumber").asText());
                    asset.setAssignee(node.path("owner").asText());
                    asset.setJurisdiction(node.path("office").asText());
                    asset.setPatentOffice("TMVIEW");
                    asset.setStatus(node.path("status").asText());

                    String appDateStr = node.path("applicationDate").asText();
                    if (!appDateStr.isEmpty()) {
                        try {
                            asset.setApplicationDate(LocalDate.parse(appDateStr, DateTimeFormatter.ISO_LOCAL_DATE));
                        } catch (Exception e) {
                            log.debug("Could not parse date: {}", appDateStr);
                        }
                    }

                    String regDateStr = node.path("registrationDate").asText();
                    if (!regDateStr.isEmpty()) {
                        try {
                            asset.setGrantDate(LocalDate.parse(regDateStr, DateTimeFormatter.ISO_LOCAL_DATE));
                        } catch (Exception e) {
                            log.debug("Could not parse date: {}", regDateStr);
                        }
                    }

                    assets.add(asset);
                }
            }

            long totalResults = root.path("totalResults").asLong();

            SearchResultDto result = new SearchResultDto();
            result.setAssets(assets);
            result.setTotalElements(totalResults);
            result.setTotalPages((int) Math.ceil((double) totalResults / request.getSize()));
            result.setCurrentPage(request.getPage());
            result.setPageSize(request.getSize());
            result.setHasNext(request.getPage() < (totalResults / request.getSize()));
            result.setHasPrevious(request.getPage() > 0);
            result.setSearchQuery(request.getQuery());
            result.setDataSource("TMVIEW");

            return result;
        } catch (Exception e) {
            log.error("Error parsing TMView search response", e);
            return createEmptyResult(request);
        }
    }

    private IpAssetDto parseAssetDetails(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);

            IpAssetDto asset = new IpAssetDto();
            asset.setExternalId(root.path("applicationNumber").asText());
            asset.setTitle(root.path("markVerbatim").asText());
            asset.setDescription(root.path("goodsServices").asText());
            asset.setAssetType(IpAsset.AssetType.TRADEMARK);
            asset.setApplicationNumber(root.path("applicationNumber").asText());
            asset.setAssignee(root.path("owner").asText());
            asset.setJurisdiction(root.path("office").asText());
            asset.setPatentOffice("TMVIEW");
            asset.setStatus(root.path("status").asText());

            String appDateStr = root.path("applicationDate").asText();
            if (!appDateStr.isEmpty()) {
                try {
                    asset.setApplicationDate(LocalDate.parse(appDateStr));
                } catch (Exception e) {
                    log.debug("Could not parse date: {}", appDateStr);
                }
            }

            String regDateStr = root.path("registrationDate").asText();
            if (!regDateStr.isEmpty()) {
                try {
                    asset.setGrantDate(LocalDate.parse(regDateStr));
                } catch (Exception e) {
                    log.debug("Could not parse date: {}", regDateStr);
                }
            }

            return asset;
        } catch (Exception e) {
            log.error("Error parsing TMView asset details", e);
            return null;
        }
    }

    private SearchResultDto createEmptyResult(SearchRequestDto request) {
        SearchResultDto result = new SearchResultDto();
        result.setAssets(new ArrayList<>());
        result.setTotalElements(0);
        result.setTotalPages(0);
        result.setCurrentPage(request.getPage());
        result.setPageSize(request.getSize());
        result.setHasNext(false);
        result.setHasPrevious(false);
        result.setSearchQuery(request.getQuery());
        result.setDataSource("TMVIEW");
        return result;
    }
}
