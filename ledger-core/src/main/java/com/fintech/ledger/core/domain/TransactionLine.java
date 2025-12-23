package com.fintech.ledger.core.domain;

import com.fintech.common.domain.Money;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Currency;

@Entity
@Table(name = "transaction_lines")
@Getter
@Setter
@NoArgsConstructor
public class TransactionLine {

    public enum Type {
        DEBIT, CREDIT
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "journal_entry_id")
    private JournalEntry journalEntry;

    @ManyToOne(optional = false)
    @JoinColumn(name = "account_id")
    private Account account;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Type type;

    // Decomposed Money fields
    @Column(name = "amount", precision = 30, scale = 18, nullable = false)
    private BigDecimal amountValue;

    @Column(name = "currency", length = 3, nullable = false)
    private String currencyCode;

    public Money getAmount() {
        return new Money(amountValue, Currency.getInstance(currencyCode));
    }

    public void setAmount(Money money) {
        this.amountValue = money.getAmount();
        this.currencyCode = money.getCurrency().getCurrencyCode();
    }
}
