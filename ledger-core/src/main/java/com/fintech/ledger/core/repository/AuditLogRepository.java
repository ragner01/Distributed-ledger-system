package com.fintech.ledger.core.repository;

import com.fintech.ledger.core.audit.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    
    Page<AuditLog> findByAction(String action, Pageable pageable);
    
    Page<AuditLog> findByUserName(String userName, Pageable pageable);
    
    Page<AuditLog> findByTimestampBetween(Instant start, Instant end, Pageable pageable);
    
    @Query("SELECT a FROM AuditLog a WHERE a.action = :action AND a.timestamp BETWEEN :start AND :end")
    List<AuditLog> findByActionAndTimestampBetween(
        @Param("action") String action,
        @Param("start") Instant start,
        @Param("end") Instant end
    );
    
    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.errorMessage IS NOT NULL AND a.timestamp >= :since")
    long countErrorsSince(@Param("since") Instant since);
}



