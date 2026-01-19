package com.ipintelligence.repo;

import com.ipintelligence.dto.MonthlyAssetCount;
import com.ipintelligence.dto.StatusMetricDTO;
import com.ipintelligence.dto.TechCount;
import com.ipintelligence.model.IpAsset;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface IpAssetRepository extends JpaRepository<IpAsset, Long> {
	Optional<IpAsset> findByExternalId(String externalId);
	
    Optional<IpAsset> findByExternalIdAndPatentOffice(String externalId, String patentOffice);

    Page<IpAsset> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    Page<IpAsset> findByInventorContainingIgnoreCase(String inventor, Pageable pageable);

    Page<IpAsset> findByAssigneeContainingIgnoreCase(String assignee, Pageable pageable);

    Page<IpAsset> findByJurisdiction(String jurisdiction, Pageable pageable);

    Page<IpAsset> findByAssetType(IpAsset.AssetType assetType, Pageable pageable);

    Page<IpAsset> findByPatentOffice(String patentOffice, Pageable pageable);

    @Query("SELECT i FROM IpAsset i WHERE "
            + "(:title IS NULL OR UPPER(i.title) LIKE UPPER(CONCAT('%', :title, '%'))) AND "
            + "(:inventor IS NULL OR UPPER(i.inventor) LIKE UPPER(CONCAT('%', :inventor, '%'))) AND "
            + "(:assignee IS NULL OR UPPER(i.assignee) LIKE UPPER(CONCAT('%', :assignee, '%'))) AND "
            + "(:jurisdiction IS NULL OR i.jurisdiction = :jurisdiction) AND "
            + "(:assetType IS NULL OR i.assetType = :assetType) AND "
            + "(:patentOffice IS NULL OR i.patentOffice = :patentOffice) AND "
            + "(:fromDate IS NULL OR i.applicationDate >= :fromDate) AND "
            + "(:toDate IS NULL OR i.applicationDate <= :toDate)")
    Page<IpAsset> searchWithFilters(@Param("title") String title,
            @Param("inventor") String inventor,
            @Param("assignee") String assignee,
            @Param("jurisdiction") String jurisdiction,
            @Param("assetType") IpAsset.AssetType assetType,
            @Param("patentOffice") String patentOffice,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            Pageable pageable);

    @Query("SELECT i FROM IpAsset i WHERE "
            + "UPPER(i.title) LIKE UPPER(CONCAT('%', :searchTerm, '%')) OR "
            + "UPPER(i.description) LIKE UPPER(CONCAT('%', :searchTerm, '%')) OR "
            + "UPPER(i.inventor) LIKE UPPER(CONCAT('%', :searchTerm, '%')) OR "
            + "UPPER(i.assignee) LIKE UPPER(CONCAT('%', :searchTerm, '%')) OR "
            + "UPPER(i.keywords) LIKE UPPER(CONCAT('%', :searchTerm, '%'))")
    Page<IpAsset> searchByKeyword(@Param("searchTerm") String searchTerm, Pageable pageable);

    List<IpAsset> findByExpiryDateBetween(LocalDate startDate, LocalDate endDate);

    @Query("SELECT DISTINCT i.jurisdiction FROM IpAsset i WHERE i.jurisdiction IS NOT NULL")
    List<String> findDistinctJurisdictions();

    @Query("SELECT DISTINCT i.patentOffice FROM IpAsset i WHERE i.patentOffice IS NOT NULL")
    List<String> findDistinctPatentOffices();

    @Query("SELECT COUNT(i) FROM IpAsset i")
    long countAllAssets();

    @Query("SELECT COUNT(i) FROM IpAsset i WHERE i.assetType = :assetType")
    long countByAssetType(@Param("assetType") IpAsset.AssetType assetType);

    // Alternative search method using native query if needed
    @Query(value = "SELECT * FROM ip_assets i WHERE "
            + "(:searchTerm IS NULL OR "
            + "UPPER(i.title) LIKE UPPER(CONCAT('%', :searchTerm, '%')) OR "
            + "UPPER(i.description) LIKE UPPER(CONCAT('%', :searchTerm, '%')) OR "
            + "UPPER(i.inventor) LIKE UPPER(CONCAT('%', :searchTerm, '%')) OR "
            + "UPPER(i.assignee) LIKE UPPER(CONCAT('%', :searchTerm, '%')) OR "
            + "UPPER(i.keywords) LIKE UPPER(CONCAT('%', :searchTerm, '%')))",
            nativeQuery = true)
    Page<IpAsset> searchByKeywordNative(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    @Query(
    	    "SELECT " +
    	    " TO_CHAR(i.applicationDate, 'YYYY-MM') AS month, " +
    	    " SUM(CASE WHEN i.assetType = 'PATENT' THEN 1 ELSE 0 END) AS patents, " +
    	    " SUM(CASE WHEN i.assetType = 'TRADEMARK' THEN 1 ELSE 0 END) AS trademarks " +
    	    "FROM IpAsset i " +
    	    "WHERE i.applicationDate IS NOT NULL " +
    	    "GROUP BY TO_CHAR(i.applicationDate, 'YYYY-MM') " +
    	    "ORDER BY month"
    	)
    	List<MonthlyAssetCount> fetchMonthlyAssetStats();

    @Query(
    	    "SELECT " +
    	    " SUBSTRING(i.ipcClassification, 1, 4) AS technology, " +
    	    " COUNT(i) AS count " +
    	    "FROM IpAsset i " +
    	    "WHERE i.ipcClassification IS NOT NULL " +
    	    "GROUP BY SUBSTRING(i.ipcClassification, 1, 4) " +
    	    "ORDER BY count DESC"
    	)
    	List<TechCount> fetchTechnologyTrends();
    
    
  

        @Query("""
            SELECT new com.ipintelligence.dto.StatusMetricDTO(
                i.status, COUNT(i)
            )
            FROM IpAsset i
            GROUP BY i.status
        """)
        List<StatusMetricDTO> fetchLifecycleMetrics();
   


    
    
    
    
    
    
}
