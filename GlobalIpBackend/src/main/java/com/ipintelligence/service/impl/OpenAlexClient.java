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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class OpenAlexClient implements PatentOfficeApiClient {

    @Value("${openalex.email:[email protected]}")
    private String email;

    private final RestTemplate restTemplate;

    public OpenAlexClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public SearchResultDto search(SearchRequestDto request) {
        log.info("Searching OpenAlex for query: {}", request.getQuery());

        SearchResultDto result = new SearchResultDto();
        result.setDataSource("OPENALEX");
        result.setSearchQuery(request.getQuery());
        result.setCurrentPage(request.getPage());
        result.setPageSize(request.getSize());

        try {
            String url = UriComponentsBuilder
                    .fromHttpUrl("https://api.openalex.org/works")
                    .queryParam("search", request.getQuery())
                    .queryParam("per-page", Math.min(request.getSize(), 25))
                    .queryParam("page", request.getPage() + 1)
                    .queryParam("mailto", email)
                    .toUriString();

            log.debug("OpenAlex URL: {}", url);

            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response != null && response.containsKey("results")) {
                List<Map<String, Object>> results =
                        (List<Map<String, Object>>) response.get("results");

                List<IpAssetDto> patents = new ArrayList<>();

                for (Map<String, Object> item : results) {
                    try {
                        IpAssetDto patent = convertToDto(item);
                        patents.add(patent);
                    } catch (Exception e) {
                        log.warn("Failed to convert OpenAlex item: {}", e.getMessage());
                    }
                }

                result.setAssets(patents);

                Map<String, Object> meta = (Map<String, Object>) response.get("meta");
                if (meta != null) {
                    Integer count = (Integer) meta.get("count");
                    result.setTotalElements(count != null ? count : patents.size());
                } else {
                    result.setTotalElements(patents.size());
                }

                result.setTotalPages((int) Math.ceil((double) result.getTotalElements() / request.getSize()));
                result.setHasNext(request.getPage() < result.getTotalPages() - 1);
                result.setHasPrevious(request.getPage() > 0);

                log.info("OpenAlex returned {} results", patents.size());
            } else {
                log.warn("No results in OpenAlex response");
                result.setAssets(new ArrayList<>());
                result.setTotalElements(0);
            }

            return result;

        } catch (Exception e) {
            log.error("Error searching via OpenAlex: {}", e.getMessage(), e);
            result.setAssets(new ArrayList<>());
            result.setTotalElements(0);
            return result;
        }
    }

    @Override
    public IpAssetDto getAssetDetails(String externalId) {
        log.info("Fetching OpenAlex asset: {}", externalId);

        try {
            String url = "https://api.openalex.org/works/" + externalId +
                    "?mailto=" + email;

            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response != null) {
                return convertToDto(response);
            }
        } catch (Exception e) {
            log.error("Error fetching OpenAlex details: {}", e.getMessage(), e);
        }

        return null;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public String getDataSource() {
        return "OPENALEX";
    }

    @Override
    public int getRateLimitPerMinute() {
        return 600;
    }

    private IpAssetDto convertToDto(Map<String, Object> item) {
        IpAssetDto dto = new IpAssetDto();

        dto.setTitle(getStringValue(item, "title"));
        dto.setDescription(getStringValue(item, "abstract"));
        dto.setExternalId(getStringValue(item, "id"));

        String pubDate = getStringValue(item, "publication_date");
        if (pubDate != null) {
            try {
                dto.setPublicationDate(LocalDate.parse(pubDate));
            } catch (Exception e) {
                log.warn("Failed to parse date: {}", pubDate);
            }
        }

        if (item.containsKey("primary_location")) {
            Map<String, Object> location = (Map<String, Object>) item.get("primary_location");
            if (location != null && location.containsKey("source")) {
                Map<String, Object> source = (Map<String, Object>) location.get("source");
                if (source != null) {
                    dto.setAssignee(getStringValue(source, "display_name"));
                }
            }
        }

        if (item.containsKey("authorships")) {
            List<Map<String, Object>> authors =
                    (List<Map<String, Object>>) item.get("authorships");
            if (authors != null && !authors.isEmpty()) {
                StringBuilder inventors = new StringBuilder();
                for (Map<String, Object> authorship : authors) {
                    Map<String, Object> author =
                            (Map<String, Object>) authorship.get("author");
                    if (author != null) {
                        String name = getStringValue(author, "display_name");
                        if (name != null) {
                            if (inventors.length() > 0) inventors.append(", ");
                            inventors.append(name);
                        }
                    }
                }
                dto.setInventor(inventors.toString());
            }
        }

        String doi = getStringValue(item, "doi");
        if (doi != null) {
            dto.setPublicationNumber(doi.replace("https://doi.org/", ""));
        }

        dto.setAssetType(IpAsset.AssetType.PATENT);
        dto.setStatus("Published");
        dto.setPatentOffice("OPENALEX");
        dto.setJurisdiction("GLOBAL");

        if (item.containsKey("concepts")) {
            List<Map<String, Object>> concepts =
                    (List<Map<String, Object>>) item.get("concepts");
            if (concepts != null && !concepts.isEmpty()) {
                StringBuilder keywords = new StringBuilder();
                int count = 0;
                for (Map<String, Object> concept : concepts) {
                    if (count >= 5) break;
                    String name = getStringValue(concept, "display_name");
                    if (name != null) {
                        if (keywords.length() > 0) keywords.append("; ");
                        keywords.append(name);
                        count++;
                    }
                }
                dto.setKeywords(keywords.toString());
            }
        }

        return dto;
    }

    private String getStringValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : null;
    }
}
