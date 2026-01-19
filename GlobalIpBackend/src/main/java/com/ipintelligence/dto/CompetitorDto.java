package com.ipintelligence.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CompetitorDto {
    private String company;
    private int filings;
    private int grants;
    private int pending;
    private String trend;
}
