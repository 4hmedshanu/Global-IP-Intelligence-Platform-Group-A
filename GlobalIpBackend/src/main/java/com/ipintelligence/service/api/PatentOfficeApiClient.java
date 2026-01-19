package com.ipintelligence.service.api;

import com.ipintelligence.dto.IpAssetDto;
import com.ipintelligence.dto.SearchRequestDto;
import com.ipintelligence.dto.SearchResultDto;

public interface PatentOfficeApiClient {

    String getDataSource();

    SearchResultDto search(SearchRequestDto searchRequest);

    IpAssetDto getAssetDetails(String externalId);

    boolean isAvailable();

    int getRateLimitPerMinute();
}