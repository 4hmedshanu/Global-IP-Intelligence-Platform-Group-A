package com.ipintelligence.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.ipintelligence.model.SystemLog;

@Repository
public interface SystemLogRepository extends JpaRepository<SystemLog, Long> {

    @Query(
      value = """
        SELECT * FROM system_logs
        ORDER BY created_at DESC
        LIMIT 50
      """,
      nativeQuery = true
    )
    List<SystemLog> findRecentLogs();
}
