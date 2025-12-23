package com.fintech.common.exception;

public class InvalidTransactionException extends LedgerException {

    public InvalidTransactionException(String message) {
        super("INVALID_TRANSACTION", message);
    }

    public InvalidTransactionException(String message, Throwable cause) {
        super("INVALID_TRANSACTION", message, cause);
    }
}



