
    package com.ipintelligence.repo;

import com.ipintelligence.model.SearchHistory;
import com.ipintelligence.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository

public interface SearchHistoryRepository extends JpaRepository<SearchHistory, Long> {

    long countByUserId(Long userId);

    Page<SearchHistory> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    List<SearchHistory> findTop10ByUserOrderByCreatedAtDesc(User user);

    List<SearchHistory> findByUserAndCreatedAtBetween(User user, LocalDateTime startDate, LocalDateTime endDate);

    void deleteByUserAndCreatedAtBefore(User user, LocalDateTime cutoffDate);

    long countByUser(User user);

    // Added for analyst dashboard search counts
    long countByUserAndDataSource(User user, String dataSource);
    long countByUserAndDataSourceAndSearchType(User user, String dataSource, SearchHistory.SearchType searchType);
    long countByUserAndSearchType(User user, SearchHistory.SearchType searchType);


    // Count where dataSource contains 'GOOGLE_PATENT' or 'USPTO' (case-insensitive)
    @Query("SELECT COUNT(sh) FROM SearchHistory sh WHERE sh.user = :user AND (LOWER(sh.dataSource) LIKE LOWER(CONCAT('%', :patent1, '%')) OR LOWER(sh.dataSource) LIKE LOWER(CONCAT('%', :patent2, '%')))")
    long countPatentSearches(@Param("user") User user, @Param("patent1") String patent1, @Param("patent2") String patent2);

    // Count where dataSource contains 'TMVIEW' (case-insensitive)
    @Query("SELECT COUNT(sh) FROM SearchHistory sh WHERE sh.user = :user AND LOWER(sh.dataSource) LIKE LOWER(CONCAT('%', :tmview, '%'))")
    long countTrademarkSearches(@Param("user") User user, @Param("tmview") String tmview);
    
    @Query(value = """
    	    SELECT DATE(created_at), COUNT(*)
    	    FROM search_history
    	    GROUP BY DATE(created_at)
    	    ORDER BY DATE(created_at)
    	""", nativeQuery = true)
    	List<Object[]> countApiCallsPerDay();
    	
    	
    	 @Modifying
    	    @Query("DELETE FROM SearchHistory sh WHERE sh.user.id = :userId")
    	    void deleteByUserId(@Param("userId") Integer userId);


}