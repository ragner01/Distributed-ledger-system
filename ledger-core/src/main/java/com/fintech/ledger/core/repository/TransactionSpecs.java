package com.fintech.ledger.core.repository;

import com.fintech.ledger.core.domain.JournalEntry;
import com.fintech.ledger.core.domain.TransactionLine;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.Instant;

public class TransactionSpecs {

    public static Specification<JournalEntry> currencyEquals(String currencyCode) {
        return (root, query, cb) -> {
            query.distinct(true);
            Join<JournalEntry, TransactionLine> lines = root.join("lines");
            return cb.equal(lines.get("currencyCode"), currencyCode);
        };
    }

    public static Specification<JournalEntry> amountGreaterThan(BigDecimal amount) {
        return (root, query, cb) -> {
            query.distinct(true);
            Join<JournalEntry, TransactionLine> lines = root.join("lines");
            return cb.greaterThan(lines.get("amountValue"), amount);
        };
    }

    public static Specification<JournalEntry> recentTransactions(Instant since) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("timestamp"), since);
    }
}
