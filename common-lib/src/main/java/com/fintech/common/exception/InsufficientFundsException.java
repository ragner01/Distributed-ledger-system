package com.fintech.common.exception;

public class InsufficientFundsException extends LedgerException {

    public InsufficientFundsException(Long accountId) {
        super("INSUFFICIENT_FUNDS", String.format("Insufficient funds for account %d", accountId));
    }

    public InsufficientFundsException(Long accountId, String details) {
        super("INSUFFICIENT_FUNDS", String.format("Insufficient funds for account %d: %s", accountId, details));
    }
}



