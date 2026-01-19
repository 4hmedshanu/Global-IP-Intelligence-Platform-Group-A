package com.ipintelligence.controller;

import com.ipintelligence.dto.CompetitorDto;
import com.ipintelligence.service.impl.CompetitorService;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/analyst/competitors")

public class CompetitorController {

	@Autowired
    CompetitorService service;

    @GetMapping
    public List<CompetitorDto> getCompetitors() {
        return service.getCompetitors();
    }
}
