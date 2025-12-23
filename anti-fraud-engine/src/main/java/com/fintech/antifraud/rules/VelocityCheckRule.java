package com.fintech.antifraud.rules;

import reactor.core.publisher.Mono;

import java.time.Duration;

public class VelocityCheckRule implements FraudRule {
    @Override
    public Mono<RuleResult> evaluate(TransactionContext context) {
        // Simulating a fast check (e.g., Redis lookup)
        // In reality, this would query a state store
        return Mono.just(RuleResult.PASSED)
                .delayElement(Duration.ofMillis(5)); // Simulate ~5ms latency
    }
}
