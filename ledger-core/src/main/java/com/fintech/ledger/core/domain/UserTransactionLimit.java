package com.fintech.ledger.core.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Instant;

/**
 * Tracks daily transaction limits per user and currency.
 */
@Entity
@Table(name = "user_transaction_limits",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "transaction_date", "currency_code"}))
@Getter
@Setter
@NoArgsConstructor
public class UserTransactionLimit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, length = 255)
    private String userId;

    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;

    @Column(name = "transaction_count", nullable = false)
    private Integer transactionCount = 0;

    @Column(name = "total_amount", precision = 30, scale = 18, nullable = false)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "currency_code", length = 3, nullable = false)
    private String currencyCode;

    @Column(name = "last_updated", nullable = false)
    private Instant lastUpdated;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        this.lastUpdated = Instant.now();
    }

    public UserTransactionLimit(String userId, LocalDate transactionDate, String currencyCode) {
        this.userId = userId;
        this.transactionDate = transactionDate;
        this.currencyCode = currencyCode;
    }
}



