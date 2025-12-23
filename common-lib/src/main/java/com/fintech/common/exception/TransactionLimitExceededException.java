package com.fintech.common.exception;

import java.math.BigDecimal;

public class TransactionLimitExceededException extends LedgerException {

    public TransactionLimitExceededException(String limitType, String details) {
        super("TRANSACTION_LIMIT_EXCEEDED", 
            String.format("Transaction limit exceeded (%s): %s", limitType, details));
    }

    public TransactionLimitExceededException(String userId, int countLimit, int currentCount) {
        super("TRANSACTION_LIMIT_EXCEEDED",
            String.format("User %s has exceeded daily transaction count limit. Limit: %d, Current: %d",
                userId, countLimit, currentCount));
    }

    public TransactionLimitExceededException(String userId, BigDecimal amountLimit, BigDecimal currentAmount, String currency) {
        super("TRANSACTION_LIMIT_EXCEEDED",
            String.format("User %s has exceeded daily transaction amount limit. Limit: %s %s, Current: %s %s",
                userId, amountLimit, currency, currentAmount, currency));
    }
}



