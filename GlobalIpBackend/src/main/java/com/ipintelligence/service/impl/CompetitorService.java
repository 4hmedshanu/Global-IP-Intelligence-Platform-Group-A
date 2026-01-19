package com.ipintelligence.service.impl;



import com.ipintelligence.dto.CompetitorDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CompetitorService {

    private final BigQueryCompetitorService bigQueryService;

    private List<CompetitorDto> staticCompetitors() {
        return List.of(
            new CompetitorDto("Google", 320, 210, 110, "Upward"),
            new CompetitorDto("Microsoft", 280, 190, 90, "Stable"),
            new CompetitorDto("Amazon", 240, 150, 90, "Growing")
        );
    }

    public List<CompetitorDto> getCompetitors() {

        List<CompetitorDto> dynamic = bigQueryService.fetchCompetitors();

        if (dynamic != null && !dynamic.isEmpty()) {
            return dynamic;
        }

        return staticCompetitors(); // fallback
    }
}
