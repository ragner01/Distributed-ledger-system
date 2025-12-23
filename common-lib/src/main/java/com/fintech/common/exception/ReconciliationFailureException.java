package com.fintech.common.exception;

public class ReconciliationFailureException extends LedgerException {

    public ReconciliationFailureException(String message) {
        super("RECONCILIATION_FAILURE", message);
    }

    public ReconciliationFailureException(String message, Throwable cause) {
        super("RECONCILIATION_FAILURE", message, cause);
    }
}



