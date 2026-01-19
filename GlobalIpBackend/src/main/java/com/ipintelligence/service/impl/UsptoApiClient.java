package com.ipintelligence.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ipintelligence.dto.IpAssetDto;
import com.ipintelligence.dto.SearchRequestDto;
import com.ipintelligence.dto.SearchResultDto;
import com.ipintelligence.model.IpAsset;
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
public class UsptoApiClient implements PatentOfficeApiClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.api.uspto.base-url:https://developer.uspto.gov/ds-api}")
    private String baseUrl;

    @Value("${app.api.uspto.api-key:}")
    private String apiKey;

    public UsptoApiClient(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public String getDataSource() {
        return "USPTO";
    }

    @Override
    public SearchResultDto search(SearchRequestDto searchRequest) {
        try {
            String url = buildSearchUrl(searchRequest);
            HttpHeaders headers = createHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            log.info("Searching USPTO API with URL: {}", url);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            return parseSearchResponse(response.getBody(), searchRequest);
        } catch (Exception e) {
            log.error("Error searching USPTO API", e);
            return createEmptyResult(searchRequest);
        }
    }

    @Override
    public IpAssetDto getAssetDetails(String externalId) {
        try {
            String url = baseUrl + "/patents/" + externalId;
            HttpHeaders headers = createHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            return parseAssetDetails(response.getBody());
        } catch (Exception e) {
            log.error("Error fetching USPTO asset details for ID: {}", externalId, e);
            return null;
        }
    }

    @Override
    public boolean isAvailable() {
        try {
            String url = baseUrl + "/patents?searchText=test&start=0&rows=1";
            HttpHeaders headers = createHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.warn("USPTO API is not available", e);
            return false;
        }
    }

    @Override
    public int getRateLimitPerMinute() {
        return 60; // USPTO typical rate limit
    }

    private String buildSearchUrl(SearchRequestDto request) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(baseUrl + "/patents")
                .queryParam("start", request.getPage() * request.getSize())
                .queryParam("rows", request.getSize());

        if (request.getQuery() != null && !request.getQuery().isEmpty()) {
            builder.queryParam("searchText", request.getQuery());
        }

        if (request.getTitle() != null && !request.getTitle().isEmpty()) {
            builder.queryParam("title", request.getTitle());
        }

        if (request.getInventor() != null && !request.getInventor().isEmpty()) {
            builder.queryParam("inventor", request.getInventor());
        }

        if (request.getAssignee() != null && !request.getAssignee().isEmpty()) {
            builder.queryParam("assignee", request.getAssignee());
        }

        if (request.getFromDate() != null) {
            builder.queryParam("dateStart", request.getFromDate().format(DateTimeFormatter.ISO_LOCAL_DATE));
        }

        if (request.getToDate() != null) {
            builder.queryParam("dateEnd", request.getToDate().format(DateTimeFormatter.ISO_LOCAL_DATE));
        }

        return builder.build().toUriString();
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("User-Agent", "IP-Intelligence-App/1.0");

        if (apiKey != null && !apiKey.isEmpty()) {
            headers.add("X-API-Key", apiKey);
        }

        return headers;
    }

    private SearchResultDto parseSearchResponse(String responseBody, SearchRequestDto request) {
        SearchResultDto result = new SearchResultDto();
        result.setDataSource(getDataSource());
        result.setSearchQuery(request.getQuery());
        result.setCurrentPage(request.getPage());
        result.setPageSize(request.getSize());

        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode results = root.path("results");

            List<IpAssetDto> assets = new ArrayList<>();
            if (results.isArray()) {
                for (JsonNode patentNode : results) {
                    IpAssetDto asset = parsePatentNode(patentNode);
                    if (asset != null) {
                        assets.add(asset);
                    }
                }
            }

            result.setAssets(assets);
            result.setTotalElements(root.path("totalHits").asLong());
            result.setTotalPages((int) Math.ceil((double) result.getTotalElements() / request.getSize()));
            result.setHasNext(request.getPage() < result.getTotalPages() - 1);
            result.setHasPrevious(request.getPage() > 0);

        } catch (Exception e) {
            log.error("Error parsing USPTO search response", e);
        }

        return result;
    }

    private IpAssetDto parsePatentNode(JsonNode patentNode) {
        try {
            IpAssetDto asset = new IpAssetDto();
            asset.setExternalId(patentNode.path("patentNumber").asText());
            asset.setTitle(patentNode.path("title").asText());
            asset.setDescription(patentNode.path("abstract").asText());
            asset.setAssetType(IpAsset.AssetType.PATENT);
            asset.setApplicationNumber(patentNode.path("applicationNumber").asText());
            asset.setPublicationNumber(patentNode.path("publicationNumber").asText());
            asset.setJurisdiction("US");
            asset.setPatentOffice("USPTO");

            // Parse dates
            String appDate = patentNode.path("applicationDate").asText();
            if (!appDate.isEmpty()) {
                asset.setApplicationDate(LocalDate.parse(appDate, DateTimeFormatter.ISO_LOCAL_DATE));
            }

            String pubDate = patentNode.path("publicationDate").asText();
            if (!pubDate.isEmpty()) {
                asset.setPublicationDate(LocalDate.parse(pubDate, DateTimeFormatter.ISO_LOCAL_DATE));
            }

            // Parse inventor
            JsonNode inventors = patentNode.path("inventors");
            if (inventors.isArray() && inventors.size() > 0) {
                StringBuilder inventorNames = new StringBuilder();
                for (JsonNode inventor : inventors) {
                    if (inventorNames.length() > 0) inventorNames.append(", ");
                    inventorNames.append(inventor.path("name").asText());
                }
                asset.setInventor(inventorNames.toString());
            }

            // Parse assignee
            JsonNode assignees = patentNode.path("assignees");
            if (assignees.isArray() && assignees.size() > 0) {
                asset.setAssignee(assignees.get(0).path("name").asText());
            }

            return asset;
        } catch (Exception e) {
            log.error("Error parsing patent node", e);
            return null;
        }
    }

    private IpAssetDto parseAssetDetails(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            return parsePatentNode(root);
        } catch (Exception e) {
            log.error("Error parsing USPTO asset details", e);
            return null;
        }
    }

    private SearchResultDto createEmptyResult(SearchRequestDto request) {
        SearchResultDto result = new SearchResultDto();
        result.setDataSource(getDataSource());
        result.setSearchQuery(request.getQuery());
        result.setCurrentPage(request.getPage());
        result.setPageSize(request.getSize());
        result.setAssets(new ArrayList<>());
        result.setTotalElements(0);
        result.setTotalPages(0);
        result.setHasNext(false);
        result.setHasPrevious(false);
        return result;
    }
}