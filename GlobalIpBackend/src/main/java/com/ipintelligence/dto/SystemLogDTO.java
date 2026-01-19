package com.ipintelligence.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SystemLogDTO {
    private Long id;
    private String level;
    private String service;
    private String message;
    private String ip;
    private String timestamp;
}


