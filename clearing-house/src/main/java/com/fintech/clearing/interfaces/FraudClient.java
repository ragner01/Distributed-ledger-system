package com.fintech.clearing.interfaces;

import com.fintech.common.domain.Money;

public interface FraudClient {
    boolean verifyTransaction(String userId, Money amount, String targetAccount);
}



