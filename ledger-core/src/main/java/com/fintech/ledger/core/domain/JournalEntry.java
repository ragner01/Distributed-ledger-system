package com.fintech.ledger.core.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "journal_entries")
@Getter
@Setter
@NoArgsConstructor
public class JournalEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false, updatable = false)
    private Instant timestamp;

    @OneToMany(mappedBy = "journalEntry", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TransactionLine> lines = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.timestamp = Instant.now();
        validate();
    }

    @PreUpdate
    protected void onUpdate() {
        validate();
    }

    public void addLine(TransactionLine line) {
        lines.add(line);
        line.setJournalEntry(this);
    }

    public void validate() {
        if (lines.size() < 2) {
            throw new IllegalStateException("JournalEntry must have at least 2 transaction lines.");
        }

        BigDecimal totalDebits = BigDecimal.ZERO;
        BigDecimal totalCredits = BigDecimal.ZERO;

        for (TransactionLine line : lines) {
            if (line.getType() == TransactionLine.Type.DEBIT) {
                totalDebits = totalDebits.add(line.getAmount().getAmount());
            } else {
                totalCredits = totalCredits.add(line.getAmount().getAmount());
            }
        }

        if (totalDebits.compareTo(totalCredits) != 0) {
            throw new IllegalStateException(
                    "JournalEntry is not balanced. Debits: " + totalDebits + ", Credits: " + totalCredits);
        }
    }
}
