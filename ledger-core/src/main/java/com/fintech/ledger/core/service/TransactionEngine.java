package com.fintech.ledger.core.service;

import com.fintech.common.audit.Auditable;
import com.fintech.common.domain.IdempotencyKey;
import com.fintech.common.domain.Money;
import com.fintech.common.exception.AccountClosedException;
import com.fintech.common.exception.AccountFrozenException;
import com.fintech.common.exception.AccountNotFoundException;
import com.fintech.common.exception.CurrencyMismatchException;
import com.fintech.common.exception.DuplicateTransactionException;
import com.fintech.common.exception.InsufficientFundsException;
import com.fintech.common.exception.InvalidTransactionException;
import com.fintech.common.validation.TransactionValidator;
import com.fintech.ledger.core.domain.Account;
import com.fintech.ledger.core.domain.JournalEntry;
import com.fintech.ledger.core.domain.TransactionIdempotency;
import com.fintech.ledger.core.domain.Account.AccountStatus;
import com.fintech.ledger.core.domain.TransactionLine;
import com.fintech.ledger.core.metrics.LedgerMetrics;
import com.fintech.ledger.core.repository.JournalEntryRepository;
import com.fintech.ledger.core.repository.TransactionIdempotencyRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionEngine {

    private final JournalEntryRepository journalEntryRepository;
    private final TransactionIdempotencyRepository idempotencyRepository;
    private final EntityManager entityManager;
    private final LedgerMetrics metrics;
    private final TransactionLimitService limitService;

    public record LegRequest(Long accountId, TransactionLine.Type type, Money amount) {
    }

    /**
     * Executes an Atomic Multi-Leg Transaction with idempotency support.
     * Enforces Pre-flight checks before saving.
     * 
     * @param idempotencyKey Unique key to prevent duplicate processing
     * @param description Transaction description
     * @param legs Transaction legs
     * @param userId User ID for transaction limits (optional, can be null)
     * @return The created JournalEntry ID
     */
    @Auditable(action = "POST_TRANSACTION")
    @Transactional(timeout = 30) // 30 second timeout
    public Long postTransaction(IdempotencyKey idempotencyKey, String description, List<LegRequest> legs, String userId) {
        long startTime = System.currentTimeMillis();
        
        try {
            // 0. Validate inputs
            TransactionValidator.validateDescription(description);
            TransactionValidator.validateLegCount(legs.size());
            legs.forEach(leg -> TransactionValidator.validateAmount(leg.amount()));

            // 0.5. Check transaction limits (if user is provided)
            if (userId != null && !legs.isEmpty()) {
                // Use first leg's amount for limit checking (assuming all legs same currency)
                limitService.checkAndUpdateLimits(userId, legs.get(0).amount());
            }

            // 1. Check idempotency - prevent duplicate processing
            String keyValue = idempotencyKey.getValue();
            idempotencyRepository.findByIdempotencyKey(keyValue)
                .ifPresent(existing -> {
                    metrics.recordDuplicateTransaction();
                    throw new DuplicateTransactionException(keyValue, existing.getJournalEntryId());
                });

        // 2. Pre-flight Check (Simulation)
        preFlightCheck(legs);

        // 3. Build Entities with optimistic locking
        JournalEntry entry = new JournalEntry();
        entry.setDescription(description);

        for (LegRequest leg : legs) {
            // Use pessimistic lock to prevent concurrent balance updates
            Account account = entityManager.find(Account.class, leg.accountId(), LockModeType.PESSIMISTIC_WRITE);
            if (account == null) {
                throw new AccountNotFoundException(leg.accountId());
            }

            // Check account status
            if (account.getStatus() == AccountStatus.FROZEN) {
                throw new AccountFrozenException(leg.accountId());
            }
            if (account.getStatus() == AccountStatus.CLOSED) {
                throw new AccountClosedException(leg.accountId());
            }

            TransactionLine line = new TransactionLine();
            line.setAccount(account);
            line.setType(leg.type());
            line.setAmount(leg.amount()); // Money VO handles Rounding/Precision

            // Update Balance directly here (or domain logic)
            // For now, implementing simple balance update logic
            BigDecimal currentBalance = account.getBalanceAmount();
            BigDecimal change = leg.amount().getAmount(); // Positive magnitude

            // Logic:
            // If Account is Liability (Wallet): Credit +, Debit -
            // If Account is Asset: Debit +, Credit -
            // Assuming Wallet/Liability model for "User Accounts"
            if (leg.type() == TransactionLine.Type.CREDIT) {
                account.setBalance(new Money(currentBalance.add(change), leg.amount().getCurrency()));
            } else {
                account.setBalance(new Money(currentBalance.subtract(change), leg.amount().getCurrency()));
            }

            entry.addLine(line);
        }

        // 4. Commit (Cascades to Lines)
        // JournalEntry.validate() will be called by @PrePersist / @PreUpdate ensuring
        // Double Entry
        journalEntryRepository.save(entry);
        
        // 5. Record idempotency to prevent duplicates
        TransactionIdempotency idempotency = new TransactionIdempotency(keyValue, entry.getId());
        idempotencyRepository.save(idempotency);

            log.info("Transaction Committed: {} (Journal Entry: {}, Idempotency Key: {})", 
                entry.getDescription(), entry.getId(), keyValue);
            
            // Record metrics
            metrics.recordTransaction();
            metrics.recordTransactionTime(System.currentTimeMillis() - startTime);
            
            return entry.getId();
        } catch (Exception e) {
            metrics.recordTransactionError();
            throw e;
        }
    }

    /**
     * Convenience method without user ID (no transaction limits applied).
     */
    @Auditable(action = "POST_TRANSACTION")
    @Transactional(timeout = 30)
    public Long postTransaction(IdempotencyKey idempotencyKey, String description, List<LegRequest> legs) {
        return postTransaction(idempotencyKey, description, legs, null);
    }

    /**
     * Legacy method for backward compatibility - generates idempotency key automatically.
     * @deprecated Use postTransaction(IdempotencyKey, String, List, String) instead
     */
    @Deprecated
    @Auditable(action = "POST_TRANSACTION")
    @Transactional(timeout = 30)
    public void postTransaction(String description, List<LegRequest> legs) {
        IdempotencyKey key = IdempotencyKey.generate();
        postTransaction(key, description, legs);
    }

    /**
     * Simulates the transaction in memory to check for violations (e.g. Negative
     * Balance, Currency Mismatch).
     * Does NOT persist changes.
     */
    private void preFlightCheck(List<LegRequest> legs) {
        log.debug("Running Pre-flight simulation...");
        Map<Long, BigDecimal> simulatedBalances = new HashMap<>();

        for (LegRequest leg : legs) {
            Account account = entityManager.find(Account.class, leg.accountId());
            if (account == null) {
                throw new AccountNotFoundException(leg.accountId());
            }

            // Currency validation: Ensure transaction currency matches account currency
            String accountCurrency = account.getBalanceCurrency();
            String transactionCurrency = leg.amount().getCurrency().getCurrencyCode();
            if (!accountCurrency.equals(transactionCurrency)) {
                throw new CurrencyMismatchException(accountCurrency, transactionCurrency);
            }

            BigDecimal current = simulatedBalances.getOrDefault(
                    leg.accountId(),
                    account.getBalanceAmount());

            BigDecimal change = leg.amount().getAmount(); // Magnitude

            // Apply logic
            if (leg.type() == TransactionLine.Type.CREDIT) {
                current = current.add(change);
            } else {
                current = current.subtract(change);
            }

            // Constraint: No Negative Balances allowed for Wallets
            if (current.compareTo(BigDecimal.ZERO) < 0) {
                throw new InsufficientFundsException(leg.accountId(),
                        String.format("Current balance would be %s after transaction", current));
            }

            simulatedBalances.put(leg.accountId(), current);
        }
        log.debug("Pre-flight Check Passed.");
    }
}
