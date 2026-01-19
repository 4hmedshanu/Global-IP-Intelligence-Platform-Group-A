package com.ipintelligence.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.ipintelligence.service.impl.LandscapeService;

import java.security.Principal;
import java.util.*;

@RestController
@RequestMapping("/api/analyst")
@CrossOrigin
public class AnalystAnalystLandscapeController {
	

	
	@Autowired
    private LandscapeService landscapeService;

	@GetMapping("/landscape")
	public Map<String, Object> getLandscapeForUser(Principal principal) {

	    if (principal == null) {
	        throw new RuntimeException("User not logged in");
	    }

	    return landscapeService.getLandscapeDataForUser(principal.getName());
	}


}
