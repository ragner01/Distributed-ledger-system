package com.fintech.ledger.core.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateAccountRequest {
    
    @NotBlank(message = "Account name is required")
    private String name;
    
    @NotNull(message = "Initial balance is required")
    @Positive(message = "Initial balance must be positive")
    private BigDecimal initialBalance;
    
    @NotBlank(message = "Currency code is required")
    private String currencyCode;
}



