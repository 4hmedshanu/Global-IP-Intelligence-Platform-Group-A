package com.ipintelligence.service.impl;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.bigquery.*;
import com.ipintelligence.dto.CompetitorDto;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class BigQueryCompetitorService {

    private BigQuery bigQuery;

    public BigQueryCompetitorService() {
        try {
            this.bigQuery = BigQueryOptions.newBuilder()
                    .setCredentials(
                            GoogleCredentials.fromStream(
                                    new FileInputStream(
                                            "src/main/resources/ip-intelligence-platformtwo-2424a952fb39.json"
                                    )
                            )
                    )
                    .build()
                    .getService();
        } catch (Exception e) {
            this.bigQuery = null;
        }
    }

    public List<CompetitorDto> fetchCompetitors() {

        if (bigQuery == null) return List.of();

        String query = """
            SELECT
              assignee AS company,
              COUNT(*) AS filings,
              SUM(CASE WHEN legal_status = 'GRANTED' THEN 1 ELSE 0 END) AS grants,
              SUM(CASE WHEN legal_status = 'APPLICATION' THEN 1 ELSE 0 END) AS pending
            FROM `ip-intelligence.patents.patent_data`
            WHERE assignee IS NOT NULL
            GROUP BY assignee
            ORDER BY filings DESC
            LIMIT 10
        """;

        try {
            QueryJobConfiguration config =
                    QueryJobConfiguration.newBuilder(query).build();

            TableResult result = bigQuery.query(config);

            List<CompetitorDto> list = new ArrayList<>();

            for (FieldValueList row : result.iterateAll()) {

                int filings = (int) row.get("filings").getLongValue();
                int grants  = (int) row.get("grants").getLongValue();
                int pending = (int) row.get("pending").getLongValue();

                list.add(new CompetitorDto(
                        row.get("company").getStringValue(),
                        filings,
                        grants,
                        pending,
                        calculateTrend(filings)
                ));
            }
            return list;

        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    private String calculateTrend(int filings) {
        if (filings > 300) return "Upward";
        if (filings > 150) return "Stable";
        return "Growing";
    }
}
