package com.ipintelligence.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ipintelligence.dto.SystemLogDTO;
import com.ipintelligence.repo.SystemLogRepository;

@Service
public class SystemLogService {

    @Autowired
    private SystemLogRepository repository;

    public List<SystemLogDTO> getRecentLogs() {
        return repository.findRecentLogs()
            .stream()
            .map(log -> new SystemLogDTO(
                log.getId(),
                log.getLevel(),
                log.getService(),
                log.getMessage(),
                log.getIp(),
                log.getCreatedAt().toString()
            ))
            .toList();
    }
}

