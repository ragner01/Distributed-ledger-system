package com.fintech.common.exception;

public class AccountClosedException extends LedgerException {

    public AccountClosedException(Long accountId) {
        super("ACCOUNT_CLOSED", String.format("Account %d is closed and cannot process transactions", accountId));
    }

    public AccountClosedException(String accountName) {
        super("ACCOUNT_CLOSED", String.format("Account '%s' is closed and cannot process transactions", accountName));
    }
}



