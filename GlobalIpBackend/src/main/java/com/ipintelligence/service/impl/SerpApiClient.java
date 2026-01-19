package com.ipintelligence.service.impl;

import com.ipintelligence.dto.IpAssetDto;
import com.ipintelligence.dto.SearchRequestDto;
import com.ipintelligence.dto.SearchResultDto;
import com.ipintelligence.model.IpAsset;
import com.ipintelligence.service.api.PatentOfficeApiClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@Slf4j
public class SerpApiClient implements PatentOfficeApiClient {

    @Value("${serpapi.api.key:d4c3d504e1ca414330d3ab50decba203a299f3d508a2ea5efba8efba365439b2}")
    private String apiKey;

    private final RestTemplate restTemplate;

    public SerpApiClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public SearchResultDto search(SearchRequestDto request) {
        String effectiveQuery = request.getQuery();
        if (request.getSearchType() != null) {
            String type = request.getSearchType().toLowerCase();
            if (type.equals("inventor") && request.getInventor() != null && !request.getInventor().isEmpty()) {
                effectiveQuery = request.getInventor();
            } else if (type.equals("assignee") && request.getAssignee() != null && !request.getAssignee().isEmpty()) {
                effectiveQuery = request.getAssignee();
            }
        }

        log.info("Searching Google Patents via SerpApi for query: {}", effectiveQuery);

        SearchResultDto result = new SearchResultDto();
        result.setDataSource("SERPAPI");
        result.setSearchQuery(effectiveQuery);
        result.setCurrentPage(request.getPage());
        result.setPageSize(request.getSize());

        if (apiKey == null || apiKey.isEmpty()) {
            log.warn("SerpApi API key is missing");
            result.setAssets(new ArrayList<>());
            result.setTotalElements(0);
            return result;
        }

        try {
            // SerpApi requires num to be between 10-100
            int serpApiNum = Math.max(10, Math.min(100, request.getSize()));

            String url = UriComponentsBuilder
                    .fromHttpUrl("https://serpapi.com/search")
                    .queryParam("engine", "google_patents")
                    .queryParam("q", effectiveQuery)
                    .queryParam("api_key", apiKey)
                    .queryParam("start", request.getPage() * serpApiNum)
                    .queryParam("num", serpApiNum)
                    .toUriString();

            log.debug("SerpApi URL: {}", url);

            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response != null && response.containsKey("organic_results")) {
                List<Map<String, Object>> organicResults
                        = (List<Map<String, Object>>) response.get("organic_results");

                List<IpAssetDto> patents = new ArrayList<>();

                // Limit to requested size
                int limit = Math.min(request.getSize(), organicResults.size());
                for (int i = 0; i < limit; i++) {
                    try {
                        IpAssetDto patent = convertToDto(organicResults.get(i));
                        patents.add(patent);
                    } catch (Exception e) {
                        log.warn("Failed to convert patent item: {}", e.getMessage());
                    }
                }

                result.setAssets(patents);
                result.setTotalElements(patents.size());
                result.setTotalPages((int) Math.ceil((double) patents.size() / request.getSize()));
                result.setHasNext(false);
                result.setHasPrevious(request.getPage() > 0);

                log.info("SerpApi returned {} results", patents.size());
            } else {
                log.warn("No organic_results found in SerpApi response");
                result.setAssets(new ArrayList<>());
                result.setTotalElements(0);
            }

            return result;

        } catch (Exception e) {
            log.error("Error searching via SerpApi: {}", e.getMessage(), e);
            result.setAssets(new ArrayList<>());
            result.setTotalElements(0);
            return result;
        }
    }

    @Override
    public IpAssetDto getAssetDetails(String externalId) {
        log.info("Fetching asset details from SerpApi for: {}", externalId);

        try {
            String url = UriComponentsBuilder
                    .fromHttpUrl("https://serpapi.com/search")
                    .queryParam("engine", "google_patents")
                    .queryParam("q", externalId)
                    .queryParam("api_key", apiKey)
                    .queryParam("num", 10) // ✅ Changed from 1 to 10
                    .toUriString();

            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response != null && response.containsKey("organic_results")) {
                List<Map<String, Object>> results
                        = (List<Map<String, Object>>) response.get("organic_results");

                if (!results.isEmpty()) {
                    return convertToDto(results.get(0));
                }
            }
        } catch (Exception e) {
            log.error("Error fetching asset details from SerpApi: {}", e.getMessage(), e);
        }

        return null;
    }

    @Override
    public boolean isAvailable() {
        return apiKey != null && !apiKey.isEmpty();
    }

    @Override
    public String getDataSource() {
        return "SERPAPI";
    }

    @Override
    public int getRateLimitPerMinute() {
        return 30;
    }

    private IpAssetDto convertToDto(Map<String, Object> item) {
        IpAssetDto dto = new IpAssetDto();

        // Basic fields
        dto.setTitle(getStringValue(item, "title"));
        dto.setDescription(getStringValue(item, "snippet"));
        dto.setPublicationNumber(getStringValue(item, "publication_number"));
        dto.setExternalId(getStringValue(item, "patent_id"));

        // If patent_id is null, use publication_number
        if (dto.getExternalId() == null) {
            dto.setExternalId(dto.getPublicationNumber());
        }

        // Inventor and Assignee
        dto.setInventor(getStringValue(item, "inventor"));
        dto.setAssignee(getStringValue(item, "assignee"));

        // Dates
        dto.setPublicationDate(parseDate(getStringValue(item, "publication_date")));
        dto.setPriorityDate(parseDate(getStringValue(item, "priority_date")));
        dto.setApplicationDate(parseDate(getStringValue(item, "filing_date")));
        dto.setGrantDate(parseDate(getStringValue(item, "grant_date")));

        // Application number
        dto.setApplicationNumber(getStringValue(item, "application_number"));

        // Classifications
        String cpc = getStringValue(item, "cpc");
        dto.setCpcClassification(cpc);
        dto.setIpcClassification(cpc);
        dto.setClassification(cpc);

        // Additional metadata
        dto.setAssetType(IpAsset.AssetType.PATENT);
        dto.setStatus("Published");
        dto.setPatentOffice("SERPAPI");
        dto.setJurisdiction(extractJurisdiction(dto.getPublicationNumber()));

        // PDF link and other metadata in keywords field
        StringBuilder keywords = new StringBuilder();
        if (item.containsKey("pdf")) {
            keywords.append("PDF: ").append(item.get("pdf")).append("; ");
        }
        if (item.containsKey("family_id")) {
            keywords.append("Family ID: ").append(item.get("family_id"));
        }
        dto.setKeywords(keywords.toString());

        return dto;
    }

    private String getStringValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : null;
    }

    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return null;
        }

        try {
            // Try format: "Oct 17, 2002"
            DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH);
            return LocalDate.parse(dateStr, formatter1);
        } catch (DateTimeParseException e1) {
            try {
                // Try format: "2002-10-17"
                return LocalDate.parse(dateStr);
            } catch (DateTimeParseException e2) {
                try {
                    // Try format: "20021017" (YYYYMMDD)
                    DateTimeFormatter formatter3 = DateTimeFormatter.ofPattern("yyyyMMdd");
                    return LocalDate.parse(dateStr, formatter3);
                } catch (DateTimeParseException e3) {
                    log.warn("Failed to parse date: {}", dateStr);
                    return null;
                }
            }
        }
    }

    private String extractJurisdiction(String publicationNumber) {
        if (publicationNumber == null || publicationNumber.isEmpty()) {
            return "UNKNOWN";
        }

        String upper = publicationNumber.toUpperCase();
        if (upper.startsWith("US")) {
            return "US";
        }
        if (upper.startsWith("EP")) {
            return "EP";
        }
        if (upper.startsWith("WO")) {
            return "WO";
        }
        if (upper.startsWith("CN")) {
            return "CN";
        }
        if (upper.startsWith("JP")) {
            return "JP";
        }
        if (upper.startsWith("KR")) {
            return "KR";
        }
        if (upper.startsWith("GB")) {
            return "GB";
        }
        if (upper.startsWith("DE")) {
            return "DE";
        }
        if (upper.startsWith("FR")) {
            return "FR";
        }

        return "UNKNOWN";
    }
}
