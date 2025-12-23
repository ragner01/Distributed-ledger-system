package com.fintech.antifraud.rules;

import reactor.core.publisher.Mono;

import java.time.Duration;

public class SanctionListRule implements FraudRule {
    @Override
    public Mono<RuleResult> evaluate(TransactionContext context) {
        // Simulating a check
        if ("SANCTIONED_USER".equals(context.userId())) {
            return Mono.just(RuleResult.REJECTED_SANCTION);
        }
        return Mono.just(RuleResult.PASSED)
                .delayElement(Duration.ofMillis(10)); // Simulate ~10ms latency
    }
}
