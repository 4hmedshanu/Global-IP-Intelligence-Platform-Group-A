package com.ipintelligence.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "ip_assets")
@Getter @Setter @NoArgsConstructor
public class IpAsset {
    public IpAsset(Long id, String externalId, String title, String description, AssetType assetType, String applicationNumber, String publicationNumber, LocalDate priorityDate, LocalDate applicationDate, LocalDate publicationDate, LocalDate grantDate, String status, String jurisdiction, String patentOffice, String inventor, String assignee, String ipcClassification, String cpcClassification, String keywords, String legalStatus, LocalDate expiryDate, String rawData, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.externalId = externalId;
        this.title = title;
        this.description = description;
        this.assetType = assetType;
        this.applicationNumber = applicationNumber;
        this.publicationNumber = publicationNumber;
        this.priorityDate = priorityDate;
        this.applicationDate = applicationDate;
        this.publicationDate = publicationDate;
        this.grantDate = grantDate;
        this.status = status;
        this.jurisdiction = jurisdiction;
        this.patentOffice = patentOffice;
        this.inventor = inventor;
        this.assignee = assignee;
        this.ipcClassification = ipcClassification;
        this.cpcClassification = cpcClassification;
        this.keywords = keywords;
        this.legalStatus = legalStatus;
        this.expiryDate = expiryDate;
        this.rawData = rawData;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "external_id", nullable = false)
    private String externalId;

    @Column(nullable = false, columnDefinition = "TEXT") // FIX: TEXT for titles
    private String title;

    @Column(columnDefinition = "TEXT") // FIX: TEXT for abstracts
    private String description;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AssetType assetType;

    @Column(name = "application_number")
    private String applicationNumber;

    @Column(name = "publication_number")
    private String publicationNumber;

    @Column(name = "priority_date")
    private LocalDate priorityDate;

    @Column(name = "application_date")
    private LocalDate applicationDate;

    @Column(name = "publication_date")
    private LocalDate publicationDate;

    @Column(name = "grant_date")
    private LocalDate grantDate;

    @Column
    private String status;

    @Column
    private String jurisdiction;

    @Column(name = "patent_office")
    private String patentOffice;

    @Column(columnDefinition = "TEXT") // FIX: TEXT for long inventor lists
    private String inventor;

    @Column(columnDefinition = "TEXT") // FIX: TEXT for assignees
    private String assignee;

    @Column(name = "ipc_classification", length = 1000)
    private String ipcClassification;

    @Column(name = "cpc_classification", length = 1000)
    private String cpcClassification;

    @Column(columnDefinition = "TEXT")
    private String keywords;

    @Column(name = "legal_status")
    private String legalStatus;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "raw_data", columnDefinition = "TEXT")
    private String rawData;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum AssetType { PATENT, TRADEMARK, DESIGN, UTILITY_MODEL }
        // Custom getters for dashboard analytics
        public String getKeywords() {
            return keywords;
        }

        public String getAssignee() {
            return assignee;
        }

        public LocalDate getApplicationDate() {
            return applicationDate;
        }
}