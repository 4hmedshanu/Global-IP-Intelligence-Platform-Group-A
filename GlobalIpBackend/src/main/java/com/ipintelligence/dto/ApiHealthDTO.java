package com.ipintelligence.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ApiHealthDTO {
    private String endpoint;
    private String status;
    private String uptime;
    private String avgResponse;
}
