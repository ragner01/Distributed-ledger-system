package com.fintech.ledger.core.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Custom metrics for ledger operations.
 */
@Component
@RequiredArgsConstructor
public class LedgerMetrics {

    private final MeterRegistry meterRegistry;

    private Counter transactionCounter;
    private Counter transactionErrorCounter;
    private Counter duplicateTransactionCounter;
    private Timer transactionProcessingTime;
    private Counter reconciliationCounter;
    private Counter reconciliationFailureCounter;

    public void initialize() {
        this.transactionCounter = Counter.builder("ledger.transactions.total")
            .description("Total number of transactions processed")
            .register(meterRegistry);

        this.transactionErrorCounter = Counter.builder("ledger.transactions.errors")
            .description("Number of failed transactions")
            .tag("type", "error")
            .register(meterRegistry);

        this.duplicateTransactionCounter = Counter.builder("ledger.transactions.duplicates")
            .description("Number of duplicate transaction attempts")
            .register(meterRegistry);

        this.transactionProcessingTime = Timer.builder("ledger.transactions.processing.time")
            .description("Transaction processing time")
            .register(meterRegistry);

        this.reconciliationCounter = Counter.builder("ledger.reconciliation.total")
            .description("Total number of reconciliation runs")
            .register(meterRegistry);

        this.reconciliationFailureCounter = Counter.builder("ledger.reconciliation.failures")
            .description("Number of reconciliation failures")
            .register(meterRegistry);
    }

    public void recordTransaction() {
        transactionCounter.increment();
    }

    public void recordTransactionError() {
        transactionErrorCounter.increment();
    }

    public void recordDuplicateTransaction() {
        duplicateTransactionCounter.increment();
    }

    public void recordTransactionTime(long durationMs) {
        transactionProcessingTime.record(durationMs, TimeUnit.MILLISECONDS);
    }

    public void recordReconciliation() {
        reconciliationCounter.increment();
    }

    public void recordReconciliationFailure() {
        reconciliationFailureCounter.increment();
    }
}



