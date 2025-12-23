package com.fintech.ledger.core.controller;

import com.fintech.common.api.ApiResponse;
import com.fintech.common.domain.IdempotencyKey;
import com.fintech.common.domain.Money;
import com.fintech.ledger.core.domain.TransactionLine;
import com.fintech.ledger.core.dto.request.PostTransactionRequest;
import com.fintech.ledger.core.dto.response.TransactionResponse;
import com.fintech.ledger.core.service.TransactionEngine;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
@Slf4j
public class TransactionController {

    private final TransactionEngine transactionEngine;

    @PostMapping
    public ResponseEntity<ApiResponse<TransactionResponse>> postTransaction(
            @Valid @RequestBody PostTransactionRequest request,
            Authentication authentication) {
        
        log.info("Posting transaction: {}", request.getDescription());
        
        // Use authenticated user ID if not provided
        String userId = request.getUserId();
        if (userId == null && authentication != null) {
            userId = authentication.getName();
        }
        
        // Convert DTO legs to service legs
        List<TransactionEngine.LegRequest> legs = request.getLegs().stream()
            .map(leg -> new TransactionEngine.LegRequest(
                leg.getAccountId(),
                leg.getType(),
                Money.of(leg.getAmount(), leg.getCurrencyCode())
            ))
            .collect(Collectors.toList());
        
        // Process transaction
        IdempotencyKey key = IdempotencyKey.of(request.getIdempotencyKey());
        Long journalEntryId = transactionEngine.postTransaction(
            key, 
            request.getDescription(), 
            legs,
            userId
        );
        
        TransactionResponse response = TransactionResponse.builder()
            .journalEntryId(journalEntryId)
            .description(request.getDescription())
            .timestamp(Instant.now())
            .idempotencyKey(request.getIdempotencyKey())
            .build();
        
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(response));
    }
}



