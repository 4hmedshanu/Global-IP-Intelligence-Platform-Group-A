package com.ipintelligence.dto;

import java.time.LocalDate;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class FilingDto {
    private int id;
    private LocalDate date;
    private String status;
    private String description;
}
