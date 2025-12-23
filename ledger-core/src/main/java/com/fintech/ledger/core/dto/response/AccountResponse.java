package com.fintech.ledger.core.dto.response;

import com.fintech.ledger.core.domain.Account;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
public class AccountResponse {
    private Long id;
    private String name;
    private BigDecimal balanceAmount;
    private String balanceCurrency;
    private Account.AccountStatus status;
    private Instant createdAt;
    
    public static AccountResponse from(Account account) {
        return AccountResponse.builder()
            .id(account.getId())
            .name(account.getName())
            .balanceAmount(account.getBalanceAmount())
            .balanceCurrency(account.getBalanceCurrency())
            .status(account.getStatus())
            .createdAt(account.getCreatedAt())
            .build();
    }
}



