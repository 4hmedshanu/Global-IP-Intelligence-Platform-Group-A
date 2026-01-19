package com.ipintelligence.dto;

public class StatusMetricDTO {
    private String status;
    private Long count;

    public StatusMetricDTO(String status, Long count) {
        this.status = status;
        this.count = count;
    }

    public String getStatus() { return status; }
    public Long getCount() { return count; }
}
