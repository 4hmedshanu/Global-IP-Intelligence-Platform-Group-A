package com.ipintelligence.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class SystemUsageDTO {
    private String date;
    private Long users;
    private Long apiCalls;
    
    
    
    
}

