package com.ipintelligence.service;

import com.google.cloud.bigquery.*;
import com.google.auth.oauth2.GoogleCredentials;
import com.ipintelligence.dto.IpAssetDto;
import com.ipintelligence.dto.SearchRequestDto;
import com.ipintelligence.dto.SearchResultDto;
import com.ipintelligence.model.IpAsset;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class GooglePatentService {

    @Value("${google.bigquery.project-id}")
    private String projectId;

    @Value("${google.credentials.file}")
    private String credentialsPath;

    private BigQuery bigquery;

    private BigQuery getBigQueryClient() {
        if (bigquery != null) {
            return bigquery;
        }

        try (FileInputStream serviceAccountStream = new FileInputStream(credentialsPath)) {
            GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccountStream);
            bigquery = BigQueryOptions.newBuilder()
                    .setProjectId(projectId)
                    .setCredentials(credentials)
                    .build()
                    .getService();
            log.info("BigQuery client initialized successfully for project: {}", projectId);
            return bigquery;
        } catch (IOException e) {
            log.error("Failed to load Google credentials for BigQuery from: {}", credentialsPath, e);
            throw new RuntimeException("Failed to load Google credentials for BigQuery", e);
        }
    }

    // ✅ CORRECTED METHOD
    public SearchResultDto search(SearchRequestDto searchRequest) {

        log.info("[PATENT-DEBUG] GooglePatentService search: fromDate={}, toDate={}, query={}, inventor={}, assignee={}, jurisdiction={}",
                searchRequest.getFromDate(),
                searchRequest.getToDate(),
                searchRequest.getQuery(),
                searchRequest.getInventor(),
                searchRequest.getAssignee(),
                searchRequest.getJurisdiction());

        SearchResultDto result = new SearchResultDto();
        result.setDataSource("GOOGLE_PATENT");
        result.setSearchQuery(searchRequest.getQuery());
        result.setCurrentPage(searchRequest.getPage());
        result.setPageSize(searchRequest.getSize());

        try {
            BigQuery client = getBigQueryClient();
            String query = buildQuery(searchRequest);

            log.info("[PATENT-DEBUG] Executing BigQuery SQL: {}", query);

            // ✅ BUILD QUERY CONFIG WITH CONDITIONAL PARAMETERS
            QueryJobConfiguration.Builder configBuilder = QueryJobConfiguration.newBuilder(query);

            if (searchRequest.getQuery() != null && !searchRequest.getQuery().isEmpty()) {
                String keywordParam = "%" + searchRequest.getQuery().toLowerCase() + "%";
                configBuilder.addNamedParameter("keyword", QueryParameterValue.string(keywordParam));
                log.info("[PATENT-DEBUG] Keyword param: {}", keywordParam);
            }

            if (searchRequest.getInventor() != null && !searchRequest.getInventor().isEmpty()) {
                String inventorParam = "%" + searchRequest.getInventor().toLowerCase() + "%";
                configBuilder.addNamedParameter("inventor", QueryParameterValue.string(inventorParam));
                log.info("[PATENT-DEBUG] Inventor param: {}", inventorParam);
            }

            if (searchRequest.getAssignee() != null && !searchRequest.getAssignee().isEmpty()) {
                String assigneeParam = "%" + searchRequest.getAssignee().toLowerCase() + "%";
                configBuilder.addNamedParameter("assignee", QueryParameterValue.string(assigneeParam));
                log.info("[PATENT-DEBUG] Assignee param: {}", assigneeParam);
            }

            if (searchRequest.getJurisdiction() != null && !searchRequest.getJurisdiction().isEmpty()) {
                String jurisdictionParam = searchRequest.getJurisdiction().toUpperCase();
                configBuilder.addNamedParameter("jurisdiction", QueryParameterValue.string(jurisdictionParam));
                log.info("[PATENT-DEBUG] Jurisdiction param: {}", jurisdictionParam);
            }

            QueryJobConfiguration queryConfig = configBuilder.build();

            TableResult queryResult = client.query(queryConfig);
            List<IpAssetDto> patents = new ArrayList<>();

            int count = 0;
            for (FieldValueList row : queryResult.iterateAll()) {
                patents.add(convertRowToDto(row));
                count++;
            }

            result.setAssets(patents);
            result.setTotalElements(count);
            result.setTotalPages((int) Math.ceil((double) count / searchRequest.getSize()));
            result.setHasNext(false);
            result.setHasPrevious(false);

            log.info("BigQuery search returned {} results", count);
            return result;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("BigQuery search interrupted", e);
            result.setAssets(new ArrayList<>());
            result.setTotalElements(0);
            return result;
        } catch (Exception e) {
            log.error("Error executing BigQuery search: {}", e.getMessage(), e);
            result.setAssets(new ArrayList<>());
            result.setTotalElements(0);
            return result;
        }
    }

    public IpAssetDto getAssetDetails(String publicationNumber) {
        log.info("Fetching patent details for: {}", publicationNumber);

        try {
            BigQuery client = getBigQueryClient();

            String query = """
                SELECT
                  publication_number,
                  title_localized,
                  abstract_localized,
                  application_number,
                  filing_date,
                  publication_date,
                  assignee_harmonized,
                  inventor_harmonized,
                  cpc,
                  ipc,
                  priority_date,
                  family_id,
                  country_code
                FROM `patents-public-data.patents.publications`
                WHERE publication_number = @pubNumber
                LIMIT 1
            """;

            QueryJobConfiguration queryConfig = QueryJobConfiguration.newBuilder(query)
                    .addNamedParameter("pubNumber", QueryParameterValue.string(publicationNumber))
                    .build();

            TableResult result = client.query(queryConfig);

            for (FieldValueList row : result.iterateAll()) {
                return convertRowToDto(row);
            }

            log.warn("Patent not found: {}", publicationNumber);
            return null;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("BigQuery detail fetch interrupted", e);
            return null;
        } catch (Exception e) {
            log.error("Error fetching patent details from BigQuery: {}", e.getMessage(), e);
            return null;
        }
    }

    // ✅ CORRECTED METHOD
    private String buildQuery(SearchRequestDto searchRequest) {
        StringBuilder query = new StringBuilder("""
            SELECT
              publication_number,
              title_localized,
              abstract_localized,
              application_number,
              filing_date,
              publication_date,
              assignee_harmonized,
              inventor_harmonized,
              cpc,
              ipc,
              priority_date,
              family_id,
              country_code
            FROM `patents-public-data.patents.publications`
            WHERE 1=1
        """);

        // Add keyword search if provided
        if (searchRequest.getQuery() != null && !searchRequest.getQuery().isEmpty()) {
            query.append(" AND (");
            query.append("  EXISTS(SELECT 1 FROM UNNEST(title_localized) AS t WHERE LOWER(t.text) LIKE @keyword)");
            query.append("  OR EXISTS(SELECT 1 FROM UNNEST(abstract_localized) AS a WHERE LOWER(a.text) LIKE @keyword)");
            query.append(")");
        }

        // Add inventor search if provided
        if (searchRequest.getInventor() != null && !searchRequest.getInventor().isEmpty()) {
            query.append(" AND EXISTS(SELECT 1 FROM UNNEST(inventor_harmonized) AS inv ")
                    .append("WHERE LOWER(inv.name) LIKE @inventor)");
        }

        // Add assignee search if provided
        if (searchRequest.getAssignee() != null && !searchRequest.getAssignee().isEmpty()) {
            query.append(" AND EXISTS(SELECT 1 FROM UNNEST(assignee_harmonized) AS asg ")
                    .append("WHERE LOWER(asg.name) LIKE @assignee)");
        }

        // Add jurisdiction if provided
        if (searchRequest.getJurisdiction() != null && !searchRequest.getJurisdiction().isEmpty()) {
            query.append(" AND UPPER(country_code) = @jurisdiction");
        }

        if (searchRequest.getFromDate() != null) {
            String fromDate = searchRequest.getFromDate().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            query.append(" AND publication_date >= ").append(fromDate);
        }

        if (searchRequest.getToDate() != null) {
            String toDate = searchRequest.getToDate().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            query.append(" AND publication_date <= ").append(toDate);
        }

        int offset = searchRequest.getPage() * searchRequest.getSize();
        query.append("\nLIMIT ").append(searchRequest.getSize())
                .append(" OFFSET ").append(offset);

        return query.toString();
    }

    private IpAssetDto convertRowToDto(FieldValueList row) {
        IpAssetDto dto = new IpAssetDto();

        try {
            String pubNumber = getStringValue(row, "publication_number");
            dto.setExternalId(pubNumber);
            dto.setPublicationNumber(pubNumber);

            dto.setTitle(getLocalizedTextField(row, "title_localized"));
            dto.setDescription(getLocalizedTextField(row, "abstract_localized"));
            dto.setPatentOffice("GOOGLE_PATENT");
            dto.setAssetType(IpAsset.AssetType.PATENT);

            String countryCode = getStringValue(row, "country_code");
            dto.setJurisdiction(countryCode != null ? countryCode : "INTERNATIONAL");

            String filingDate = getStringValue(row, "filing_date");
            if (filingDate != null) {
                dto.setApplicationDate(parseDate(filingDate));
            }

            String publicationDate = getStringValue(row, "publication_date");
            if (publicationDate != null) {
                dto.setPublicationDate(parseDate(publicationDate));
            }

            String priorityDate = getStringValue(row, "priority_date");
            if (priorityDate != null) {
                dto.setPriorityDate(parseDate(priorityDate));
            }

            dto.setAssignee(getHarmonizedNameField(row, "assignee_harmonized"));
            dto.setInventor(getHarmonizedNameField(row, "inventor_harmonized"));

            String cpcClass = getClassificationCodeField(row, "cpc");
            String ipcClass = getClassificationCodeField(row, "ipc");

            dto.setCpcClassification(cpcClass);
            dto.setIpcClassification(ipcClass);

            if (cpcClass != null && !cpcClass.isEmpty()) {
                dto.setClassification(cpcClass);
            } else if (ipcClass != null && !ipcClass.isEmpty()) {
                dto.setClassification(ipcClass);
            }

            dto.setApplicationNumber(getStringValue(row, "application_number"));

            String familyId = getStringValue(row, "family_id");
            if (familyId != null) {
                dto.setKeywords("Family ID: " + familyId);
            }

            dto.setStatus("Published");
            dto.setLegalStatus("Active");

        } catch (Exception e) {
            log.error("Error converting BigQuery row to DTO: {}", e.getMessage(), e);
        }

        return dto;
    }

    private String getStringValue(FieldValueList row, String fieldName) {
        try {
            FieldValue field = row.get(fieldName);
            if (field != null && !field.isNull()) {
                return field.getStringValue();
            }
        } catch (Exception e) {
            log.debug("Error getting field {}: {}", fieldName, e.getMessage());
        }
        return null;
    }

    private String getLocalizedTextField(FieldValueList row, String fieldName) {
        try {
            FieldValue field = row.get(fieldName);
            if (field == null || field.isNull()) {
                return null;
            }

            List<FieldValue> arr = field.getRepeatedValue();
            if (arr == null || arr.isEmpty()) {
                return null;
            }

            FieldValue struct = arr.get(0);
            if (struct == null || struct.isNull()) {
                return null;
            }

            List<FieldValue> recordValues = struct.getRecordValue();
            if (recordValues != null && !recordValues.isEmpty()) {
                FieldValue textField = recordValues.get(0);
                if (textField != null && !textField.isNull()) {
                    return textField.getStringValue();
                }
            }
        } catch (Exception e) {
            log.debug("Error getting localized text field {}: {}", fieldName, e.getMessage());
        }
        return null;
    }

    private String getHarmonizedNameField(FieldValueList row, String fieldName) {
        try {
            FieldValue field = row.get(fieldName);
            if (field == null || field.isNull()) {
                return null;
            }

            List<FieldValue> arr = field.getRepeatedValue();
            if (arr == null || arr.isEmpty()) {
                return null;
            }

            StringBuilder names = new StringBuilder();
            for (int i = 0; i < Math.min(arr.size(), 3); i++) {
                FieldValue struct = arr.get(i);
                if (struct != null && !struct.isNull()) {
                    List<FieldValue> recordValues = struct.getRecordValue();
                    if (recordValues != null && !recordValues.isEmpty()) {
                        FieldValue nameField = recordValues.get(0);
                        if (nameField != null && !nameField.isNull()) {
                            if (names.length() > 0) {
                                names.append("; ");
                            }
                            names.append(nameField.getStringValue());
                        }
                    }
                }
            }
            return names.length() > 0 ? names.toString() : null;
        } catch (Exception e) {
            log.debug("Error getting harmonized name field {}: {}", fieldName, e.getMessage());
        }
        return null;
    }

    private String getClassificationCodeField(FieldValueList row, String fieldName) {
        try {
            FieldValue field = row.get(fieldName);
            if (field == null || field.isNull()) {
                return null;
            }

            List<FieldValue> arr = field.getRepeatedValue();
            if (arr == null || arr.isEmpty()) {
                return null;
            }

            StringBuilder codes = new StringBuilder();
            for (int i = 0; i < Math.min(arr.size(), 5); i++) {
                FieldValue struct = arr.get(i);
                if (struct != null && !struct.isNull()) {
                    List<FieldValue> recordValues = struct.getRecordValue();
                    if (recordValues != null && !recordValues.isEmpty()) {
                        FieldValue codeField = recordValues.get(0);
                        if (codeField != null && !codeField.isNull()) {
                            if (codes.length() > 0) {
                                codes.append(", ");
                            }
                            codes.append(codeField.getStringValue());
                        }
                    }
                }
            }
            return codes.length() > 0 ? codes.toString() : null;
        } catch (Exception e) {
            log.debug("Error getting classification code field {}: {}", fieldName, e.getMessage());
        }
        return null;
    }

    private LocalDate parseDate(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            return null;
        }

        try {
            if (dateString.length() == 8) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
                return LocalDate.parse(dateString, formatter);
            }
            return LocalDate.parse(dateString);
        } catch (Exception e) {
            log.debug("Error parsing date {}: {}", dateString, e.getMessage());
            return null;
        }
    }

    public boolean isAvailable() {
        try {
            getBigQueryClient();
            return true;
        } catch (Exception e) {
            log.warn("BigQuery service is not available: {}", e.getMessage());
            return false;
        }
    }

    public String getDataSource() {
        return "GOOGLE_PATENT";
    }
}
