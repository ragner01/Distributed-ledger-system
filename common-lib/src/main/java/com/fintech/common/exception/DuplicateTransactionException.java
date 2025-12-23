package com.fintech.common.exception;

public class DuplicateTransactionException extends LedgerException {

    public DuplicateTransactionException(String idempotencyKey) {
        super("DUPLICATE_TRANSACTION", 
            String.format("Transaction with idempotency key '%s' has already been processed", idempotencyKey));
    }

    public DuplicateTransactionException(String idempotencyKey, Long journalEntryId) {
        super("DUPLICATE_TRANSACTION", 
            String.format("Transaction with idempotency key '%s' was already processed as journal entry %d", 
                idempotencyKey, journalEntryId));
    }
}



