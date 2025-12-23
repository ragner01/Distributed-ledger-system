package com.fintech.ledger.core.jobs;

import com.fintech.common.domain.Money;
import com.fintech.common.exception.ReconciliationFailureException;
import com.fintech.ledger.core.domain.Account;
import com.fintech.ledger.core.domain.TransactionLine;
import com.fintech.ledger.core.metrics.LedgerMetrics;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class ReconciliationJob {

    @PersistenceContext
    private EntityManager entityManager;
    
    private final LedgerMetrics metrics;

    private static volatile boolean systemHalted = false;

    public static boolean isSystemHalted() {
        return systemHalted;
    }

    /**
     * Hourly Reconciliation:
     * Verifies that for every account, the current balance matches the sum of all
     * transaction lines.
     * If mismatch, HALT SYSTEM.
     */
    @Scheduled(cron = "0 0 * * * *") // Hourly
    @Transactional(readOnly = true)
    public void reconcile() {
        if (systemHalted) {
            log.warn("System is HALTED. Skipping reconciliation.");
            return;
        }

        log.info("Starting Hourly Reconciliation...");
        metrics.recordReconciliation();

        List<Account> accounts = entityManager.createQuery("SELECT a FROM Account a", Account.class).getResultList();

        for (Account account : accounts) {
            BigDecimal calculatedBalance = entityManager.createQuery(
                    "SELECT SUM(CASE WHEN tl.type = 'DEBIT' THEN -tl.amountValue ELSE tl.amountValue END) " +
                            "FROM TransactionLine tl WHERE tl.account.id = :accountId",
                    BigDecimal.class)
                    .setParameter("accountId", account.getId())
                    .getSingleResult();

            if (calculatedBalance == null) {
                calculatedBalance = BigDecimal.ZERO;
            }

            // Note: Debit decreases Asset account? Or Liability?
            // In Banking (Liability to user): Credit increases balance, Debit decreases
            // balance.
            // Assumption: Account is a User Wallet (Liability from bank perspective).
            // Credit = Deposit (+), Debit = Withdrawal (-).
            // The query above assumes Credit is positive flow to account.

            // Compare with strict precision
            if (account.getBalanceAmount().compareTo(calculatedBalance) != 0) {
                haltSystem(account, calculatedBalance);
                break;
            }
        }

        log.info("Reconciliation Completed Successfully.");
    }

    private void haltSystem(Account account, BigDecimal calculated) {
        systemHalted = true;
        metrics.recordReconciliationFailure();
        String msg = String.format(
                "CRITICAL: Ledger Integrity Failure for Account %s. STORED: %s, CALCULATED: %s. SYSTEM HALTED.",
                account.getId(), account.getBalanceAmount(), calculated);
        log.error(msg);
        // In real world: Send PagerDuty alert, Stop all writes.
        throw new ReconciliationFailureException(msg);
    }
}
