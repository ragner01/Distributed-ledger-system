package com.fintech.ledger.core.controller;

import com.fintech.common.api.ApiResponse;
import com.fintech.common.domain.IdempotencyKey;
import com.fintech.ledger.core.dto.request.CrossBorderTransferRequest;
import com.fintech.ledger.core.dto.response.TransactionResponse;
import com.fintech.ledger.core.service.CrossBorderTransferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cross-border")
@RequiredArgsConstructor
@Slf4j
public class CrossBorderController {

    private final CrossBorderTransferService crossBorderService;

    @PostMapping("/transfer")
    public ResponseEntity<ApiResponse<TransactionResponse>> executeTransfer(
            @Valid @RequestBody CrossBorderTransferRequest request,
            Authentication authentication) {
        
        log.info("Executing cross-border transfer: {} {} -> {} {}", 
            request.getAmount(), request.getSourceCurrency(),
            request.getTargetCurrency(), request.getTargetAccountId());
        
        // Use authenticated user ID if not provided
        String userId = request.getUserId();
        if (userId == null && authentication != null) {
            userId = authentication.getName();
        }
        
        // Execute transfer (note: CrossBorderTransferService needs to be updated to support userId)
        crossBorderService.executeTransfer(
            request.getSourceAccountId(),
            request.getTargetAccountId(),
            request.getAmount(),
            request.getSourceCurrency(),
            request.getTargetCurrency()
        );
        
        TransactionResponse response = TransactionResponse.builder()
            .description(String.format("FX Transfer %s to %s", 
                request.getSourceCurrency(), request.getTargetCurrency()))
            .timestamp(Instant.now())
            .idempotencyKey(UUID.randomUUID().toString())
            .build();
        
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(response));
    }
}



