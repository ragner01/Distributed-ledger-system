package com.fintech.ledger.core.repository;

import com.fintech.ledger.core.domain.UserTransactionLimit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface UserTransactionLimitRepository extends JpaRepository<UserTransactionLimit, Long> {
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM UserTransactionLimit u WHERE u.userId = :userId AND u.transactionDate = :date AND u.currencyCode = :currency")
    Optional<UserTransactionLimit> findByUserIdAndDateAndCurrency(
        @Param("userId") String userId,
        @Param("date") LocalDate date,
        @Param("currency") String currency
    );
}



