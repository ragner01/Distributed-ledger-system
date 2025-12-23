package com.fintech.ledger.core.repository;

import com.fintech.ledger.core.domain.TransactionIdempotency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TransactionIdempotencyRepository extends JpaRepository<TransactionIdempotency, Long> {
    Optional<TransactionIdempotency> findByIdempotencyKey(String idempotencyKey);
    boolean existsByIdempotencyKey(String idempotencyKey);
}



