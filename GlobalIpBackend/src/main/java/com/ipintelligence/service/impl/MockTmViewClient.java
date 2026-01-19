package com.ipintelligence.service.impl;

import com.ipintelligence.dto.IpAssetDto;
import com.ipintelligence.dto.SearchRequestDto;
import com.ipintelligence.dto.SearchResultDto;
import com.ipintelligence.service.api.PatentOfficeApiClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
@Slf4j
//public class MockTmViewClient implements PatentOfficeApiClient {
public class MockTmViewClient {

//    private final MockDataService mockDataService;
//
//    public MockTmViewClient(MockDataService mockDataService) {
//        this.mockDataService = mockDataService;
//    }
//
//    @Override
//    public SearchResultDto search(SearchRequestDto request) {
//        log.info("MOCK TMVIEW: Searching for query: {}", request.getQuery());
//
//        SearchResultDto result = new SearchResultDto();
//        result.setDataSource("TMVIEW_MOCK");
//        result.setSearchQuery(request.getQuery());
//        result.setCurrentPage(request.getPage());
//        result.setPageSize(request.getSize());
//
//        // Generate 2 mock results per search (fewer for trademarks)
//        int mockResultCount = Math.min(request.getSize(), 2);
//        result.setAssets(mockDataService.generateMockPatents(
//                request.getQuery(), "TMVIEW", mockResultCount
//        ));
//
//        result.setTotalElements(mockResultCount);
//        result.setTotalPages(1);
//        result.setHasNext(false);
//        result.setHasPrevious(false);
//
//        log.info("MOCK TMVIEW: Returned {} results", mockResultCount);
//
//        return result;
//    }
//
//    @Override
//    public IpAssetDto getAssetDetails(String externalId) {
//        log.info("MOCK TMVIEW: Fetching details for {}", externalId);
//        return mockDataService.generateMockPatents(externalId, "TMVIEW", 1).get(0);
//    }
//
//    @Override
//    public boolean isAvailable() {
//        return true;
//    }
//
//    @Override
//    public String getDataSource() {
//        return "TMVIEW_MOCK";
//    }
//
//    @Override
//    public int getRateLimitPerMinute() {
//        return 9999; // Mock has no limits
//    }
}
