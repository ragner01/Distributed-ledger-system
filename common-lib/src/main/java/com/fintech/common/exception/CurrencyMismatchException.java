package com.fintech.common.exception;

public class CurrencyMismatchException extends LedgerException {

    public CurrencyMismatchException(String accountCurrency, String transactionCurrency) {
        super("CURRENCY_MISMATCH", 
            String.format("Currency mismatch: Account currency is %s, but transaction uses %s", 
                accountCurrency, transactionCurrency));
    }
}



