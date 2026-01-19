package com.ipintelligence.service.impl;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.ipintelligence.dto.IpAssetDto;
import com.ipintelligence.dto.SearchRequestDto;
import com.ipintelligence.dto.SearchResultDto;
import com.ipintelligence.model.IpAsset;
import com.ipintelligence.service.api.PatentOfficeApiClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Arrays;

@Service
@Slf4j
public class EpoApiClient implements PatentOfficeApiClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final XmlMapper xmlMapper;

    @Value("${app.api.epo.base-url:https://ops.epo.org/3.2}")
    private String baseUrl;
    @Value("${app.api.epo.consumer-key:}")
    private String consumerKey;
    @Value("${app.api.epo.consumer-secret:}")
    private String consumerSecret;

    @Value("${app.api.epo.fetch-full-details:false}")
    private boolean fetchFullDetails;

    private String accessToken;
    private long tokenExpiryTime = 0;

    public EpoApiClient(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.xmlMapper = new XmlMapper();
        this.xmlMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    public String getDataSource() { return "EPO"; }

    @Override
    public SearchResultDto search(SearchRequestDto searchRequest) {
        try {
            if (!ensureAuthenticated()) return createEmptyResult(searchRequest);
            List<IpAssetDto> allAssets = new ArrayList<>();
            boolean didFallback = false;

            // Alias map for common assignees/inventors (expand as needed)
            Map<String, List<String>> aliasMap = Map.of(
                "IBM", Arrays.asList("IBM", "IBM Corporation", "International Business Machines", "International Business Machines Corporation"),
                "International Business Machines", Arrays.asList("International Business Machines", "International Business Machines Corporation", "IBM", "IBM Corporation"),
                "International Business Machines Corporation", Arrays.asList("International Business Machines Corporation", "International Business Machines", "IBM", "IBM Corporation")
                // Add more aliases as needed
            );

            boolean usedAliasExpansion = false;
            // Check if assignee or inventor matches an alias key
            String assignee = searchRequest.getAssignee();
            String inventor = searchRequest.getInventor();
            List<String> assigneeAliases = null;
            List<String> inventorAliases = null;
            if (assignee != null && aliasMap.containsKey(assignee.trim())) {
                assigneeAliases = aliasMap.get(assignee.trim());
            }
            if (inventor != null && aliasMap.containsKey(inventor.trim())) {
                inventorAliases = aliasMap.get(inventor.trim());
            }

            // If alias expansion needed, try all aliases and merge results
            boolean foundAny = false;
            if ((assigneeAliases != null && !assigneeAliases.isEmpty()) || (inventorAliases != null && !inventorAliases.isEmpty())) {
                usedAliasExpansion = true;
                List<String> aliasList = new ArrayList<>();
                if (assigneeAliases != null) aliasList.addAll(assigneeAliases);
                if (inventorAliases != null) aliasList.addAll(inventorAliases);
                java.util.HashSet<String> tried = new java.util.HashSet<>();
                for (String alias : aliasList) {
                    if (!tried.add(alias.toLowerCase())) continue; // skip duplicates
                    SearchRequestDto aliasRequest = new SearchRequestDto();
                    aliasRequest.setQuery(searchRequest.getQuery());
                    aliasRequest.setFromDate(searchRequest.getFromDate());
                    aliasRequest.setToDate(searchRequest.getToDate());
                    aliasRequest.setPage(searchRequest.getPage());
                    aliasRequest.setSize(searchRequest.getSize());
                    aliasRequest.setSortBy(searchRequest.getSortBy());
                    aliasRequest.setSortDirection(searchRequest.getSortDirection());
                    aliasRequest.setSearchType(searchRequest.getSearchType());
                    if (assigneeAliases != null) aliasRequest.setAssignee(alias);
                    if (inventorAliases != null) aliasRequest.setInventor(alias);
                    String url = buildSearchUrl(aliasRequest);
                    try {
                        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(createHeaders(null)), String.class);
                        log.info("[EPO-DEBUG] Alias '{}' response: {}", alias, response.getBody());
                        List<String> pubIds = extractPublicationIds(response.getBody());
                        if (!pubIds.isEmpty()) foundAny = true;
                        for (String pubId : pubIds) {
                            try {
                                IpAssetDto asset = fetchBiblioData(pubId);
                                if (asset != null) allAssets.add(asset);
                            } catch (Exception ex) {
                                log.warn("[EPO-DEBUG] Failed to fetch biblio for {}: {}", pubId, ex.getMessage());
                            }
                        }
                    } catch (Exception ex) {
                        log.warn("[EPO-DEBUG] Alias '{}' search failed: {}", alias, ex.getMessage());
                    }
                }
            } else {
                // Try original query only
                String url = buildSearchUrl(searchRequest);
                ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(createHeaders(null)), String.class);
                log.info("[EPO-DEBUG] Raw response: {}", response.getBody());
                List<String> pubIds = extractPublicationIds(response.getBody());
                if (!pubIds.isEmpty()) foundAny = true;
                for (String pubId : pubIds) {
                    try {
                        IpAssetDto asset = fetchBiblioData(pubId);
                        if (asset != null) allAssets.add(asset);
                    } catch (Exception ex) {
                        log.warn("[EPO-DEBUG] Failed to fetch biblio for {}: {}", pubId, ex.getMessage());
                    }
                }
            }

            // Fallback: if no results and searching by assignee/inventor, retry as keyword search
            if (!foundAny && ((assignee != null && !assignee.isEmpty()) || (inventor != null && !inventor.isEmpty()))) {
                didFallback = true;
                SearchRequestDto fallbackRequest = new SearchRequestDto();
                // Use assignee/inventor as keyword
                String keyword = assignee != null && !assignee.isEmpty() ? assignee : inventor;
                fallbackRequest.setQuery(keyword);
                fallbackRequest.setFromDate(searchRequest.getFromDate());
                fallbackRequest.setToDate(searchRequest.getToDate());
                fallbackRequest.setPage(searchRequest.getPage());
                fallbackRequest.setSize(searchRequest.getSize());
                fallbackRequest.setSortBy(searchRequest.getSortBy());
                fallbackRequest.setSortDirection(searchRequest.getSortDirection());
                fallbackRequest.setSearchType("KEYWORD");
                String url = buildSearchUrl(fallbackRequest);
                try {
                    ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(createHeaders(null)), String.class);
                    log.info("[EPO-DEBUG] Fallback keyword response: {}", response.getBody());
                    List<String> pubIds = extractPublicationIds(response.getBody());
                    for (String pubId : pubIds) {
                        try {
                            IpAssetDto asset = fetchBiblioData(pubId);
                            if (asset != null) allAssets.add(asset);
                        } catch (Exception ex) {
                            log.warn("[EPO-DEBUG] Failed to fetch biblio for {}: {}", pubId, ex.getMessage());
                        }
                    }
                } catch (Exception ex) {
                    log.warn("[EPO-DEBUG] Fallback keyword search failed: {}", ex.getMessage());
                }
            }

            // Remove duplicates by publication number
            List<IpAssetDto> uniqueAssets = new ArrayList<>();
            java.util.HashSet<String> seen = new java.util.HashSet<>();
            for (IpAssetDto asset : allAssets) {
                if (asset.getPublicationNumber() != null && seen.add(asset.getPublicationNumber())) {
                    uniqueAssets.add(asset);
                }
            }
            SearchResultDto result = createEmptyResult(searchRequest);
            result.setAssets(uniqueAssets);
            result.setTotalElements(uniqueAssets.size());
            result.setTotalPages((int) Math.ceil((double) uniqueAssets.size() / searchRequest.getSize()));
            // Optionally, you can add a flag or message to result to indicate fallback was used
            return result;
        } catch (Exception e) {
            log.error("EPO search error: {}", e.getMessage(), e);
            return createEmptyResult(searchRequest);
        }
    }

    @Override
    public IpAssetDto getAssetDetails(String fullId) {
        try {
            ensureAuthenticated();
            IpAssetDto asset = fetchBiblioData(fullId);
            if (asset != null) asset.setDescription(fetchAbstract(fullId));
            return asset;
        } catch (Exception e) {
            log.error("EPO: Details error for {}: {}", fullId, e.getMessage());
            return null;
        }
    }

    private IpAssetDto fetchBiblioData(String fullId) throws Exception {
        String url = baseUrl + "/rest-services/published-data/publication/epodoc/" + fullId + "/biblio";
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(createHeaders(null)), String.class);
        JsonNode root = xmlMapper.readTree(response.getBody());
        JsonNode doc = root.path("exchange-documents").path("exchange-document");
        if (doc.isArray()) doc = doc.get(0);
        return parsePatentNode(doc);
    }

    private String fetchAbstract(String fullId) throws Exception {
        String url = baseUrl + "/rest-services/published-data/publication/epodoc/" + fullId + "/abstract";
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(createHeaders(null)), String.class);
        JsonNode root = xmlMapper.readTree(response.getBody());
        JsonNode abs = root.path("exchange-documents").path("exchange-document").path("abstract");
        if (abs.isMissingNode()) abs = root.path("abstract");
        StringBuilder sb = new StringBuilder();
        JsonNode p = abs.path("p");
        if (p.isArray()) {
            for (JsonNode n : p) sb.append(n.isObject() ? n.path("").asText() : n.asText()).append(" ");
        } else {
            sb.append(p.isObject() ? p.path("").asText() : p.asText());
        }
        return sb.toString().trim();
    }

    private IpAssetDto parsePatentNode(JsonNode exchangeDoc) {
        JsonNode biblio = exchangeDoc.path("bibliographic-data");
        IpAssetDto asset = new IpAssetDto();
        asset.setAssetType(IpAsset.AssetType.PATENT);
        asset.setPatentOffice("EPO");

        JsonNode docId = biblio.path("publication-reference").path("document-id");
        if (docId.isArray()) docId = docId.get(0);
        asset.setJurisdiction(docId.path("country").asText());
        asset.setExternalId(docId.path("doc-number").asText());
        asset.setPublicationNumber(asset.getJurisdiction() + asset.getExternalId());

        JsonNode titleNode = biblio.path("invention-title");
        String title = "";
        if (titleNode.isArray()) {
            for (JsonNode t : titleNode) {
                if ("en".equalsIgnoreCase(t.path("lang").asText())) title = t.path("").asText();
            }
            if (title.isEmpty()) title = titleNode.get(0).path("").asText();
        } else {
            title = titleNode.isObject() ? titleNode.path("").asText() : titleNode.asText();
        }
        asset.setTitle(title);

        asset.setAssignee(extractNames(biblio.path("parties").path("applicants").path("applicant"), "applicant-name"));
        asset.setInventor(extractNames(biblio.path("parties").path("inventors").path("inventor"), "inventor-name"));
        return asset;
    }

    private String extractNames(JsonNode root, String tag) {
        if (root.isMissingNode()) return null;
        List<String> names = new ArrayList<>();
        Iterable<JsonNode> nodes = root.isArray() ? root : List.of(root);
        for (JsonNode n : nodes) {
            JsonNode nameNode = n.path(tag).path("name");
            String name = nameNode.isObject() ? nameNode.path("").asText() : nameNode.asText();
            if (name == null || name.isEmpty()) name = n.path("name").asText();
            if (name != null && !name.isEmpty()) names.add(name);
        }
        return names.isEmpty() ? null : String.join(", ", names);
    }

    private SearchResultDto parseSearchResponse(String body, SearchRequestDto request) throws Exception {
        // Deprecated: now handled in search() with full biblio fetch
        return createEmptyResult(request);
    }

    private List<String> extractPublicationIds(String body) throws Exception {
        List<String> ids = new ArrayList<>();
        JsonNode root = xmlMapper.readTree(body);
        JsonNode bibSearch = root.path("biblio-search");
        JsonNode pubs = bibSearch.path("search-result").path("publication-reference");
        if (pubs.isArray()) {
            for (JsonNode p : pubs) {
                JsonNode docId = p.path("document-id");
                if (docId.isArray()) docId = docId.get(0);
                String id = docId.path("doc-number").asText(null);
                if (id != null) ids.add(id);
            }
        } else if (!pubs.isMissingNode()) {
            JsonNode docId = pubs.path("document-id");
            if (docId.isArray()) docId = docId.get(0);
            String id = docId.path("doc-number").asText(null);
            if (id != null) ids.add(id);
        }
        return ids;
    }

    private IpAssetDto parsePublicationReference(JsonNode node) {
        JsonNode docId = node.path("document-id");
        if (docId.isArray()) docId = docId.get(0);
        IpAssetDto asset = new IpAssetDto();
        asset.setExternalId(docId.path("doc-number").asText(null));
        asset.setPatentOffice("EPO");
        asset.setPublicationNumber((docId.path("country").asText("EPO")) +
                (asset.getExternalId() != null ? asset.getExternalId() : "UNKNOWN"));
        asset.setTitle("Patent " + asset.getPublicationNumber());
        return asset;
    }

    private boolean ensureAuthenticated() {
        if (accessToken != null && System.currentTimeMillis() < tokenExpiryTime) return true;
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.setBasicAuth(consumerKey, consumerSecret);
            ResponseEntity<String> response = restTemplate.exchange(
                    "https://ops.epo.org/3.2/auth/accesstoken",
                    HttpMethod.POST,
                    new HttpEntity<>("grant_type=client_credentials", headers),
                    String.class
            );
            accessToken = objectMapper.readTree(response.getBody()).path("access_token").asText();
            tokenExpiryTime = System.currentTimeMillis() + (15 * 60 * 1000);
            return true;
        } catch (Exception e) {
            log.error("EPO authentication failed: {}", e.getMessage());
            return false;
        }
    }

    // ✅ CORRECTED METHOD
    private String buildSearchUrl(SearchRequestDto req) {
        StringBuilder queryBuilder = new StringBuilder();

        // Add keyword if provided
        if (req.getQuery() != null && !req.getQuery().isEmpty()) {
            queryBuilder.append("ta=").append(req.getQuery()).append("*");
        }

        // Add assignee if provided
        if (req.getAssignee() != null && !req.getAssignee().isEmpty()) {
            if (queryBuilder.length() > 0) queryBuilder.append(" and ");
            queryBuilder.append("pa=").append(req.getAssignee()).append("*");
        }

        // Add inventor if provided
        if (req.getInventor() != null && !req.getInventor().isEmpty()) {
            if (queryBuilder.length() > 0) queryBuilder.append(" and ");
            queryBuilder.append("in=").append(req.getInventor()).append("*");
        }

        // If nothing provided, use default
        if (queryBuilder.length() == 0) {
            queryBuilder.append("ta=patent");
        }

        String fullUrl = baseUrl + "/rest-services/published-data/search?q="
                + URLEncoder.encode(queryBuilder.toString(), StandardCharsets.UTF_8);
        log.info("[EPO-DEBUG] Search URL: {}", fullUrl);
        return fullUrl;
    }

    private HttpHeaders createHeaders(SearchRequestDto req) {
        HttpHeaders h = new HttpHeaders();
        h.set("Accept", "application/xml");
        h.set("User-Agent", "GlobalIP/1.0");
        if (req != null) {
            h.set("X-OPS-Range", (req.getPage() * req.getSize() + 1) + "-" + ((req.getPage() + 1) * req.getSize()));
        }
        if (accessToken != null) h.setBearerAuth(accessToken);
        return h;
    }

    private SearchResultDto createEmptyResult(SearchRequestDto req) {
        SearchResultDto r = new SearchResultDto();
        r.setAssets(new ArrayList<>());
        r.setDataSource("EPO");
        r.setSearchQuery(req.getQuery());
        r.setCurrentPage(req.getPage());
        r.setPageSize(req.getSize());
        return r;
    }

    @Override
    public boolean isAvailable() {
        return consumerKey != null && !consumerKey.isEmpty() && ensureAuthenticated();
    }

    @Override
    public int getRateLimitPerMinute() {
        return 30;
    }
}
