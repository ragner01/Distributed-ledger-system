package com.fintech.antifraud.rules;

import com.fintech.common.domain.Money;
import reactor.core.publisher.Mono;

// Simple DTO for transaction payload in this context
// In a real system this might be shared, but for now defining here or using a Map
// Let's assume a generic Object or a simple DTO
public interface FraudRule {
    /**
     * Evaluates a transaction asynchronously.
     * 
     * @param transactionContext Data about the transaction (amount, user, etc)
     * @return Mono emitting PASSED or a specific REJECTION reason
     */
    Mono<RuleResult> evaluate(TransactionContext transactionContext);

    // Simple context class
    record TransactionContext(String userId, Money amount, String targetAccount) {}
}
