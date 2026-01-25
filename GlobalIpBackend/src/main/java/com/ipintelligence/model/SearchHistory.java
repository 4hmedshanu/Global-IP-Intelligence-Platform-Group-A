package com.ipintelligence.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "search_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "search_query", nullable = false)
    private String searchQuery;

    @Column(name = "search_filters", columnDefinition = "TEXT")
    private String searchFilters; // JSON string of applied filters

    @Column(name = "results_count")
    private Integer resultsCount;

    @Column(name = "search_type")
    @Enumerated(EnumType.STRING)
    private SearchType searchType; // How the search was performed

    @Column(name = "asset_type") // NEW FIELD - tracks if searching for patent or trademark
    @Enumerated(EnumType.STRING)
    private AssetType assetType;

    @Column(name = "data_source")
    private String dataSource; // USPTO, EPO, WIPO, TMView, GOOGLE_PATENT

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    // Search method type
    public enum SearchType {
        KEYWORD,
        INVENTOR,
        ASSIGNEE,
        CLASSIFICATION,
        ADVANCED
    }

    // Asset type being searched
    public enum AssetType {
        PATENT,
        TRADEMARK,
        GENERAL
    }
}
