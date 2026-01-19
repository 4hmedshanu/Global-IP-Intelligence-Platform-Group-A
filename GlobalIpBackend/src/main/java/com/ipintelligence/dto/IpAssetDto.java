package com.ipintelligence.dto;

import com.ipintelligence.model.IpAsset;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class IpAssetDto {

    private Long id;
    private String url;
    // External identifiers
    private String externalId;
    private String applicationNumber;
    private String publicationNumber;

    // Core bibliographic data
    private String title;
    private String description;
    private IpAsset.AssetType assetType;
    private String status;
    private String jurisdiction;
    private String patentOffice;

    // Parties
    private String inventor;
    private String assignee;

    // Dates
    private LocalDate priorityDate;
    private LocalDate applicationDate;
    private LocalDate publicationDate;
    private LocalDate grantDate;
    private LocalDate expiryDate;

    // Classifications
    private String ipcClassification;
    private String cpcClassification;

    /**
     * Optional combined classification string (e.g. CPC + IPC) for UI/search convenience.
     * This is not persisted separately; it is derived or used as a display/helper field.
     */
    private String classification;

    // Other metadata
    private String keywords;
    private String legalStatus;

    // Audit timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
