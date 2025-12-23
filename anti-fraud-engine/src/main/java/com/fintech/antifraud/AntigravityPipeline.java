package com.fintech.antifraud;

import com.fintech.antifraud.rules.FraudRule;
import com.fintech.antifraud.rules.RuleResult;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@RequiredArgsConstructor
public class AntigravityPipeline {

    private final List<FraudRule> rules;

    /**
     * Runs all rules. If any rule rejects, the pipeline returns that rejection
     * immediately (or collects them).
     * Requirements: Fast < 50ms.
     * Strategy: Run parallel, return first rejection or PASSED if all pass.
     */
    public Mono<RuleResult> checkTransaction(FraudRule.TransactionContext context) {
        return Flux.fromIterable(rules)
                .flatMap(rule -> rule.evaluate(context))
                // If we want to fail fast on the first rejection:
                .filter(result -> result != RuleResult.PASSED)
                .next() // Get the first rejection
                .defaultIfEmpty(RuleResult.PASSED); // If stream is empty (no rejections), return PASSED
    }
}
