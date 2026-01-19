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
public class WipoApiClient implements PatentOfficeApiClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.api.wipo.base-url:https://patentscope.wipo.int/search/en/api}")
    private String baseUrl;

    @Value("${app.api.wipo.api-key:}")
    private String apiKey;

    public WipoApiClient(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public String getDataSource() {
        return "WIPO";
    }

    @Override
    public SearchResultDto search(SearchRequestDto searchRequest) {
        try {
            String url = buildSearchUrl(searchRequest);
            HttpHeaders headers = createHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            log.info("Searching WIPO API with URL: {}", url);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            return parseSearchResponse(response.getBody(), searchRequest);
        } catch (Exception e) {
            log.error("Error searching WIPO API", e);
            return createEmptyResult(searchRequest);
        }
    }

    @Override
    public IpAssetDto getAssetDetails(String externalId) {
        try {
            String url = baseUrl + "/patent/" + externalId;
            HttpHeaders headers = createHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            return parseAssetDetails(response.getBody());
        } catch (Exception e) {
            log.error("Error fetching WIPO asset details for ID: {}", externalId, e);
            return null;
        }
    }

    @Override
    public boolean isAvailable() {
        try {
            HttpHeaders headers = createHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    baseUrl + "/status",
                    HttpMethod.GET,
                    entity,
                    String.class
            );
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.warn("WIPO API is not available", e);
            return false;
        }
    }

    @Override
    public int getRateLimitPerMinute() {
        return 30;
    }

    private String buildSearchUrl(SearchRequestDto request) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(baseUrl + "/search")
                .queryParam("start", request.getPage() * request.getSize())
                .queryParam("rows", request.getSize());

        if (request.getQuery() != null && !request.getQuery().isEmpty()) {
            builder.queryParam("q", request.getQuery());
        }

        if (request.getInventor() != null && !request.getInventor().isEmpty()) {
            builder.queryParam("inventor", request.getInventor());
        }

        if (request.getAssignee() != null && !request.getAssignee().isEmpty()) {
            builder.queryParam("applicant", request.getAssignee());
        }

        if (request.getJurisdiction() != null && !request.getJurisdiction().isEmpty()) {
            builder.queryParam("country", request.getJurisdiction());
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
            headers.set("Authorization", "Bearer " + apiKey);
        }

        return headers;
    }

    private SearchResultDto parseSearchResponse(String responseBody, SearchRequestDto request) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode results = root.path("results");

            List<IpAssetDto> assets = new ArrayList<>();

            if (results.isArray()) {
                for (JsonNode node : results) {
                    IpAssetDto asset = new IpAssetDto();
                    asset.setExternalId(node.path("applicationNumber").asText());
                    asset.setTitle(node.path("title").asText());
                    asset.setDescription(node.path("abstract").asText());
                    asset.setAssetType(IpAsset.AssetType.PATENT);
                    asset.setApplicationNumber(node.path("applicationNumber").asText());
                    asset.setPublicationNumber(node.path("publicationNumber").asText());
                    asset.setInventor(node.path("applicants").asText());
                    asset.setAssignee(node.path("applicants").asText());
                    asset.setJurisdiction(node.path("country").asText());
                    asset.setPatentOffice("WIPO");
                    asset.setStatus(node.path("status").asText());

                    String appDateStr = node.path("applicationDate").asText();
                    if (!appDateStr.isEmpty()) {
                        try {
                            asset.setApplicationDate(LocalDate.parse(appDateStr));
                        } catch (Exception e) {
                            log.debug("Could not parse date: {}", appDateStr);
                        }
                    }

                    String pubDateStr = node.path("publicationDate").asText();
                    if (!pubDateStr.isEmpty()) {
                        try {
                            asset.setPublicationDate(LocalDate.parse(pubDateStr));
                        } catch (Exception e) {
                            log.debug("Could not parse date: {}", pubDateStr);
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
            result.setDataSource("WIPO");

            return result;
        } catch (Exception e) {
            log.error("Error parsing WIPO search response", e);
            return createEmptyResult(request);
        }
    }

    private IpAssetDto parseAssetDetails(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);

            IpAssetDto asset = new IpAssetDto();
            asset.setExternalId(root.path("applicationNumber").asText());
            asset.setTitle(root.path("title").asText());
            asset.setDescription(root.path("abstract").asText());
            asset.setAssetType(IpAsset.AssetType.PATENT);
            asset.setApplicationNumber(root.path("applicationNumber").asText());
            asset.setPublicationNumber(root.path("publicationNumber").asText());
            asset.setInventor(root.path("applicants").asText());
            asset.setAssignee(root.path("applicants").asText());
            asset.setJurisdiction(root.path("country").asText());
            asset.setPatentOffice("WIPO");
            asset.setStatus(root.path("status").asText());
            asset.setIpcClassification(root.path("ipcClass").asText());

            String appDateStr = root.path("applicationDate").asText();
            if (!appDateStr.isEmpty()) {
                try {
                    asset.setApplicationDate(LocalDate.parse(appDateStr));
                } catch (Exception e) {
                    log.debug("Could not parse date: {}", appDateStr);
                }
            }

            String pubDateStr = root.path("publicationDate").asText();
            if (!pubDateStr.isEmpty()) {
                try {
                    asset.setPublicationDate(LocalDate.parse(pubDateStr));
                } catch (Exception e) {
                    log.debug("Could not parse date: {}", pubDateStr);
                }
            }

            return asset;
        } catch (Exception e) {
            log.error("Error parsing WIPO asset details", e);
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
        result.setDataSource("WIPO");
        return result;
    }
}