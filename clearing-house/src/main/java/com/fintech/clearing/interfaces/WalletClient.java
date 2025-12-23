package com.fintech.clearing.interfaces;

import com.fintech.common.domain.Money;

public interface WalletClient {
    boolean reserveFunds(String accountId, Money amount);

    void releaseFunds(String accountId, Money amount);
}
