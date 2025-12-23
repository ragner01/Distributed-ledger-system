package com.fintech.ledger.core.domain;

import com.fintech.common.domain.Money;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.math.BigDecimal;
import java.util.Currency;

@Entity
@Table(name = "accounts")
@Getter
@Setter
@NoArgsConstructor
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    /**
     * Balance decomposed to avoid JPA dependency in common-lib.
     * Money is a pure value object without JPA annotations.
     */
    @Column(name = "balance_amount", precision = 30, scale = 18, nullable = false)
    private BigDecimal balanceAmount;

    @Column(name = "balance_currency", length = 3, nullable = false)
    private String balanceCurrency;

    @Version
    private Long version;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private AccountStatus status = AccountStatus.ACTIVE;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public enum AccountStatus {
        ACTIVE, FROZEN, CLOSED
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }

    public Money getBalance() {
        return new Money(balanceAmount, Currency.getInstance(balanceCurrency));
    }

    public void setBalance(Money money) {
        this.balanceAmount = money.getAmount();
        this.balanceCurrency = money.getCurrency().getCurrencyCode();
    }
}
