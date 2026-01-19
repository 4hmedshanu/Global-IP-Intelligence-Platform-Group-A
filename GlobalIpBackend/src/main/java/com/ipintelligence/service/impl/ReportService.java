package com.ipintelligence.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ipintelligence.dto.StatusMetricDTO;
import com.ipintelligence.repo.IpAssetRepository;

@Service
public class ReportService {

	@Autowired
    IpAssetRepository ipAssetRepository;

    

    public List<StatusMetricDTO> getLifecycleStatusMetrics() {
        return ipAssetRepository.fetchLifecycleMetrics();
    }
}
