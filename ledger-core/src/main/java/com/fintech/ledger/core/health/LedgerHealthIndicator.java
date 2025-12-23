package com.fintech.ledger.core.health;

import com.fintech.ledger.core.jobs.ReconciliationJob;
import com.fintech.ledger.core.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Custom health indicator for ledger-specific health checks.
 */
@Component
@RequiredArgsConstructor
public class LedgerHealthIndicator implements HealthIndicator {

    private final ReconciliationJob reconciliationJob;
    private final AuditLogRepository auditLogRepository;

    @Override
    public Health health() {
        Health.Builder builder = Health.up();

        // Check if system is halted
        if (ReconciliationJob.isSystemHalted()) {
            return Health.down()
                .withDetail("status", "SYSTEM_HALTED")
                .withDetail("reason", "Reconciliation failure detected - system halted for safety")
                .build();
        }

        // Check recent error rate
        Instant oneHourAgo = Instant.now().minus(1, ChronoUnit.HOURS);
        long errorCount = auditLogRepository.countErrorsSince(oneHourAgo);
        
        if (errorCount > 100) {
            builder = Health.down()
                .withDetail("status", "HIGH_ERROR_RATE")
                .withDetail("errors_last_hour", errorCount)
                .withDetail("message", "High error rate detected in last hour");
        } else if (errorCount > 50) {
            builder = Health.status("WARNING")
                .withDetail("status", "ELEVATED_ERROR_RATE")
                .withDetail("errors_last_hour", errorCount)
                .withDetail("message", "Elevated error rate - monitor closely");
        } else {
            builder.withDetail("errors_last_hour", errorCount);
        }

        return builder.build();
    }
}



