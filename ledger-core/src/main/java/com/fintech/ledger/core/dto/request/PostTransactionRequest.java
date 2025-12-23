package com.fintech.ledger.core.dto.request;

import com.fintech.ledger.core.domain.TransactionLine;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class PostTransactionRequest {
    
    @NotBlank(message = "Idempotency key is required")
    private String idempotencyKey;
    
    @NotBlank(message = "Description is required")
    private String description;
    
    @NotEmpty(message = "Transaction must have at least one leg")
    @Valid
    private List<TransactionLegRequest> legs;
    
    private String userId; // Optional, for transaction limits
    
    @Data
    public static class TransactionLegRequest {
        @NotNull(message = "Account ID is required")
        private Long accountId;
        
        @NotNull(message = "Transaction type is required")
        private TransactionLine.Type type;
        
        @NotNull(message = "Amount is required")
        @Positive(message = "Amount must be positive")
        private BigDecimal amount;
        
        @NotBlank(message = "Currency code is required")
        private String currencyCode;
    }
}



