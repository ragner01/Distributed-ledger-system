package com.fintech.common.exception;

/**
 * Base exception for all ledger-related errors.
 */
public class LedgerException extends RuntimeException {

    private final String errorCode;

    public LedgerException(String message) {
        super(message);
        this.errorCode = "LEDGER_ERROR";
    }

    public LedgerException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "LEDGER_ERROR";
    }

    public LedgerException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public LedgerException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}



