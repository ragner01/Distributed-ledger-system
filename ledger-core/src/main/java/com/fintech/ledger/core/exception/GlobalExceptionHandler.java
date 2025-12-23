package com.fintech.ledger.core.exception;

import com.fintech.common.api.ApiResponse;
import com.fintech.common.exception.AccountClosedException;
import com.fintech.common.exception.AccountFrozenException;
import com.fintech.common.exception.AccountNotFoundException;
import com.fintech.common.exception.CurrencyMismatchException;
import com.fintech.common.exception.InsufficientFundsException;
import com.fintech.common.exception.InvalidTransactionException;
import com.fintech.common.exception.LedgerException;
import com.fintech.common.exception.ReconciliationFailureException;
import com.fintech.common.exception.TransactionLimitExceededException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccountNotFound(AccountNotFoundException ex, WebRequest request) {
        log.warn("Account not found: {}", ex.getMessage());
        String traceId = UUID.randomUUID().toString();
        ApiResponse<Void> response = ApiResponse.error(ex.getMessage(), traceId);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(AccountFrozenException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccountFrozen(AccountFrozenException ex, WebRequest request) {
        log.warn("Account frozen: {}", ex.getMessage());
        String traceId = UUID.randomUUID().toString();
        ApiResponse<Void> response = ApiResponse.error(ex.getMessage(), traceId);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(AccountClosedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccountClosed(AccountClosedException ex, WebRequest request) {
        log.warn("Account closed: {}", ex.getMessage());
        String traceId = UUID.randomUUID().toString();
        ApiResponse<Void> response = ApiResponse.error(ex.getMessage(), traceId);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(TransactionLimitExceededException.class)
    public ResponseEntity<ApiResponse<Void>> handleTransactionLimitExceeded(TransactionLimitExceededException ex, WebRequest request) {
        log.warn("Transaction limit exceeded: {}", ex.getMessage());
        String traceId = UUID.randomUUID().toString();
        ApiResponse<Void> response = ApiResponse.error(ex.getMessage(), traceId);
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(response);
    }

    @ExceptionHandler(InsufficientFundsException.class)
    public ResponseEntity<ApiResponse<Void>> handleInsufficientFunds(InsufficientFundsException ex, WebRequest request) {
        log.warn("Insufficient funds: {}", ex.getMessage());
        String traceId = UUID.randomUUID().toString();
        ApiResponse<Void> response = ApiResponse.error(ex.getMessage(), traceId);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(CurrencyMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleCurrencyMismatch(CurrencyMismatchException ex, WebRequest request) {
        log.warn("Currency mismatch: {}", ex.getMessage());
        String traceId = UUID.randomUUID().toString();
        ApiResponse<Void> response = ApiResponse.error(ex.getMessage(), traceId);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(InvalidTransactionException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidTransaction(InvalidTransactionException ex, WebRequest request) {
        log.warn("Invalid transaction: {}", ex.getMessage());
        String traceId = UUID.randomUUID().toString();
        ApiResponse<Void> response = ApiResponse.error(ex.getMessage(), traceId);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(ReconciliationFailureException.class)
    public ResponseEntity<ApiResponse<Void>> handleReconciliationFailure(ReconciliationFailureException ex, WebRequest request) {
        log.error("Reconciliation failure: {}", ex.getMessage());
        String traceId = UUID.randomUUID().toString();
        ApiResponse<Void> response = ApiResponse.error(ex.getMessage(), traceId);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(LedgerException.class)
    public ResponseEntity<ApiResponse<Void>> handleLedgerException(LedgerException ex, WebRequest request) {
        log.error("Ledger exception: {}", ex.getMessage(), ex);
        String traceId = UUID.randomUUID().toString();
        ApiResponse<Void> response = ApiResponse.error(ex.getMessage(), traceId);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(
            MethodArgumentNotValidException ex, WebRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        log.warn("Validation errors: {}", errors);
        String traceId = UUID.randomUUID().toString();
        ApiResponse<Map<String, String>> response = ApiResponse.<Map<String, String>>builder()
                .traceId(traceId)
                .timestamp(java.time.Instant.now())
                .errorDetails("Validation failed")
                .data(errors)
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException ex, WebRequest request) {
        log.warn("Illegal argument: {}", ex.getMessage());
        String traceId = UUID.randomUUID().toString();
        ApiResponse<Void> response = ApiResponse.error(ex.getMessage(), traceId);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalState(IllegalStateException ex, WebRequest request) {
        log.warn("Illegal state: {}", ex.getMessage());
        String traceId = UUID.randomUUID().toString();
        ApiResponse<Void> response = ApiResponse.error(ex.getMessage(), traceId);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex, WebRequest request) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        String traceId = UUID.randomUUID().toString();
        ApiResponse<Void> response = ApiResponse.error("An unexpected error occurred", traceId);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}

