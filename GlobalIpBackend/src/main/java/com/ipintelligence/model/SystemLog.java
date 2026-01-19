package com.ipintelligence.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "system_logs")
@Getter
@Setter
public class SystemLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String level;     // INFO, WARN, ERROR
    private String service;   // AUTH, SEARCH, ADMIN
    private String message;
    private String ip;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}

