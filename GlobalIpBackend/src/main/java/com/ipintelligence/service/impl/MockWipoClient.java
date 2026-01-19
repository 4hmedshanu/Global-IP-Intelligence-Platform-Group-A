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
//public class MockWipoClient implements PatentOfficeApiClient {
public class MockWipoClient {

//    private final MockDataService mockDataService;
//
//    public MockWipoClient(MockDataService mockDataService) {
//        this.mockDataService = mockDataService;
//    }
//
//    @Override
//    public SearchResultDto search(SearchRequestDto request) {
//        log.info("MOCK WIPO: Searching for query: {}", request.getQuery());
//
//        SearchResultDto result = new SearchResultDto();
//        result.setDataSource("WIPO_MOCK");
//        result.setSearchQuery(request.getQuery());
//        result.setCurrentPage(request.getPage());
//        result.setPageSize(request.getSize());
//
//        // Generate 3 mock results per search
//        int mockResultCount = Math.min(request.getSize(), 3);
//        result.setAssets(mockDataService.generateMockPatents(
//                request.getQuery(), "WIPO", mockResultCount
//        ));
//
//        result.setTotalElements(mockResultCount);
//        result.setTotalPages(1);
//        result.setHasNext(false);
//        result.setHasPrevious(false);
//
//        log.info("MOCK WIPO: Returned {} results", mockResultCount);
//
//        return result;
//    }
//
//    @Override
//    public IpAssetDto getAssetDetails(String externalId) {
//        log.info("MOCK WIPO: Fetching details for {}", externalId);
//        return mockDataService.generateMockPatents(externalId, "WIPO", 1).get(0);
//    }
//
//    @Override
//    public boolean isAvailable() {
//        return true;
//    }
//
//    @Override
//    public String getDataSource() {
//        return "WIPO_MOCK";
//    }
//
//    @Override
//    public int getRateLimitPerMinute() {
//        return 9999; // Mock has no limits
//    }
}
