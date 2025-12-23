package com.fintech.ledger.core.domain;

import com.fintech.common.domain.IdempotencyKey;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Tracks processed transactions by idempotency key to prevent duplicates.
 */
@Entity
@Table(name = "transaction_idempotency", 
       uniqueConstraints = @UniqueConstraint(columnNames = "idempotency_key"))
@Getter
@Setter
@NoArgsConstructor
public class TransactionIdempotency {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "idempotency_key", nullable = false, unique = true, length = 255)
    private String idempotencyKey;

    @Column(name = "journal_entry_id", nullable = false)
    private Long journalEntryId;

    @Column(name = "processed_at", nullable = false, updatable = false)
    private Instant processedAt;

    @PrePersist
    protected void onCreate() {
        this.processedAt = Instant.now();
    }

    public TransactionIdempotency(String idempotencyKey, Long journalEntryId) {
        this.idempotencyKey = idempotencyKey;
        this.journalEntryId = journalEntryId;
    }
}



