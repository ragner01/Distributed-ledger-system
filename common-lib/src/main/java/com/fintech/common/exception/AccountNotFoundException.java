package com.fintech.common.exception;

public class AccountNotFoundException extends LedgerException {

    public AccountNotFoundException(Long accountId) {
        super("ACCOUNT_NOT_FOUND", String.format("Account with ID %d not found", accountId));
    }

    public AccountNotFoundException(String accountName) {
        super("ACCOUNT_NOT_FOUND", String.format("Account with name '%s' not found", accountName));
    }
}



