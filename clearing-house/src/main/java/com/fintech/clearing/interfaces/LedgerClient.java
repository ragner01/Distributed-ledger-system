package com.fintech.clearing.interfaces;

import com.fintech.common.domain.Money;

public interface LedgerClient {
    void commitTransaction(String fromAccount, String toAccount, Money amount);
}



