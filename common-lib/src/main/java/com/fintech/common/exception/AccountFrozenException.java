package com.fintech.common.exception;

public class AccountFrozenException extends LedgerException {

    public AccountFrozenException(Long accountId) {
        super("ACCOUNT_FROZEN", String.format("Account %d is frozen and cannot process transactions", accountId));
    }

    public AccountFrozenException(String accountName) {
        super("ACCOUNT_FROZEN", String.format("Account '%s' is frozen and cannot process transactions", accountName));
    }
}



