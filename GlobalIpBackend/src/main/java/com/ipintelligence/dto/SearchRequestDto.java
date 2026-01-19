package com.ipintelligence.dto;

import com.ipintelligence.model.IpAsset;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class SearchRequestDto {

    private String searchType; // 'keyword', 'inventor', 'assignee'
    private String query;
    private String title;
    private String inventor;
    private String assignee;
    private String jurisdiction;
    private IpAsset.AssetType assetType;
    private String patentOffice;
    private String keywords;
    private LocalDate fromDate;
    private LocalDate toDate;
    private String classification;
    private List<String> dataSources; // USPTO, EPO, WIPO, TMView
    private int page = 0;
    private int size = 20;
    private String sortBy = "applicationDate";
    private String sortDirection = "desc";
}
