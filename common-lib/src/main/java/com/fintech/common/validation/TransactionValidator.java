package com.fintech.common.validation;

import com.fintech.common.domain.Money;
import com.fintech.common.exception.InvalidTransactionException;

import java.math.BigDecimal;

/**
 * Validates transaction amounts and business rules.
 */
public class TransactionValidator {

    private static final BigDecimal MIN_AMOUNT = BigDecimal.ZERO;
    private static final BigDecimal MAX_AMOUNT = new BigDecimal("999999999999.99"); // ~1 trillion

    /**
     * Validates transaction amount is within acceptable bounds.
     */
    public static void validateAmount(Money amount) {
        if (amount == null) {
            throw new InvalidTransactionException("Transaction amount cannot be null");
        }

        BigDecimal amountValue = amount.getAmount();
        
        if (amountValue == null) {
            throw new InvalidTransactionException("Transaction amount value cannot be null");
        }

        if (amountValue.compareTo(MIN_AMOUNT) <= 0) {
            throw new InvalidTransactionException(
                String.format("Transaction amount must be greater than zero, got: %s", amountValue));
        }

        if (amountValue.compareTo(MAX_AMOUNT) > 0) {
            throw new InvalidTransactionException(
                String.format("Transaction amount exceeds maximum allowed (%s), got: %s", MAX_AMOUNT, amountValue));
        }

        // Validate currency
        if (amount.getCurrency() == null) {
            throw new InvalidTransactionException("Transaction currency cannot be null");
        }
    }

    /**
     * Validates description is not empty or malicious.
     */
    public static void validateDescription(String description) {
        if (description == null || description.isBlank()) {
            throw new InvalidTransactionException("Transaction description cannot be null or empty");
        }

        if (description.length() > 500) {
            throw new InvalidTransactionException("Transaction description exceeds maximum length of 500 characters");
        }

        // Basic XSS prevention - check for script tags
        String lowerDescription = description.toLowerCase();
        if (lowerDescription.contains("<script") || lowerDescription.contains("javascript:")) {
            throw new InvalidTransactionException("Transaction description contains potentially malicious content");
        }
    }

    /**
     * Validates number of transaction legs.
     */
    public static void validateLegCount(int legCount) {
        if (legCount < 2) {
            throw new InvalidTransactionException("Transaction must have at least 2 legs (double-entry requirement)");
        }

        if (legCount > 100) {
            throw new InvalidTransactionException("Transaction cannot have more than 100 legs");
        }
    }
}



