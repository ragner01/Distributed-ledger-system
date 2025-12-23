package com.fintech.ledger.core.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CrossBorderTransferRequest {
    
    @NotNull(message = "Source account ID is required")
    private Long sourceAccountId;
    
    @NotNull(message = "Target account ID is required")
    private Long targetAccountId;
    
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;
    
    @NotBlank(message = "Source currency is required")
    private String sourceCurrency;
    
    @NotBlank(message = "Target currency is required")
    private String targetCurrency;
    
    private String userId; // Optional, for transaction limits
}



