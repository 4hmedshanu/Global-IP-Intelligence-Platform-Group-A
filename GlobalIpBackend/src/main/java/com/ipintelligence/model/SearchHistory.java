package com.ipintelligence.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "search_history")
@Getter
@Setter
@NoArgsConstructor
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
    private SearchType searchType;

    @Column(name = "data_source")
    private String dataSource; // USPTO, EPO, WIPO, TMView

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public enum SearchType {
        KEYWORD, INVENTOR, ASSIGNEE, CLASSIFICATION, ADVANCED
    }
}