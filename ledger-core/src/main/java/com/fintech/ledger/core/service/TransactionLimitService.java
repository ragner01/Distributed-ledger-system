package com.fintech.ledger.core.service;

import com.fintech.common.domain.Money;
import com.fintech.common.exception.TransactionLimitExceededException;
import com.fintech.ledger.core.domain.UserTransactionLimit;
import com.fintech.ledger.core.repository.UserTransactionLimitRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

/**
 * Manages per-user transaction limits (daily count and amount limits).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionLimitService {

    private final UserTransactionLimitRepository limitRepository;

    @Value("${ledger.transaction.limits.daily.count:100}")
    private int dailyCountLimit;

    @Value("${ledger.transaction.limits.daily.amount:1000000.00}")
    private BigDecimal dailyAmountLimit;

    /**
     * Checks and updates transaction limits for a user.
     * Throws exception if limits are exceeded.
     */
    @Transactional
    public void checkAndUpdateLimits(String userId, Money amount) {
        LocalDate today = LocalDate.now();
        String currency = amount.getCurrency().getCurrencyCode();

        // Get or create limit record for today
        UserTransactionLimit limit = limitRepository
            .findByUserIdAndDateAndCurrency(userId, today, currency)
            .orElseGet(() -> {
                UserTransactionLimit newLimit = new UserTransactionLimit(userId, today, currency);
                return limitRepository.save(newLimit);
            });

        // Check count limit
        if (limit.getTransactionCount() >= dailyCountLimit) {
            throw new TransactionLimitExceededException(userId, dailyCountLimit, limit.getTransactionCount());
        }

        // Check amount limit
        BigDecimal newTotal = limit.getTotalAmount().add(amount.getAmount());
        if (newTotal.compareTo(dailyAmountLimit) > 0) {
            throw new TransactionLimitExceededException(userId, dailyAmountLimit, newTotal, currency);
        }

        // Update limits
        limit.setTransactionCount(limit.getTransactionCount() + 1);
        limit.setTotalAmount(newTotal);
        limitRepository.save(limit);

        log.debug("Updated transaction limits for user {}: count={}, amount={} {}", 
            userId, limit.getTransactionCount(), limit.getTotalAmount(), currency);
    }

    /**
     * Gets current daily limits for a user.
     */
    public Optional<UserTransactionLimit> getCurrentLimits(String userId, String currency) {
        return limitRepository.findByUserIdAndDateAndCurrency(userId, LocalDate.now(), currency);
    }
}



