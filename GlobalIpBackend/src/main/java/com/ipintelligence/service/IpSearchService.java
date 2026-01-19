package com.ipintelligence.service;

import com.ipintelligence.dto.IpAssetDto;
import com.ipintelligence.dto.SearchRequestDto;
import com.ipintelligence.dto.SearchResultDto;
import com.ipintelligence.model.IpAsset;
import com.ipintelligence.model.SearchHistory;
import com.ipintelligence.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IpSearchService {

    SearchResultDto searchAcrossAllSources(SearchRequestDto searchRequest, User user);

    SearchResultDto searchSpecificSource(SearchRequestDto searchRequest, String dataSource, User user);

    Page<IpAsset> searchLocalDatabase(SearchRequestDto searchRequest, Pageable pageable);

    IpAssetDto getAssetDetails(Long assetId);

    IpAssetDto getAssetDetailsByExternalId(String externalId, String dataSource);

    List<IpAssetDto> saveSearchResults(List<IpAssetDto> assets);

    List<SearchHistory> getUserSearchHistory(User user, Pageable pageable);

    void saveSearchHistory(SearchRequestDto searchRequest, SearchResultDto result, User user);

    List<String> getAvailableJurisdictions();

    List<String> getAvailablePatentOffices();

    List<String> getAvailableDataSources();
}