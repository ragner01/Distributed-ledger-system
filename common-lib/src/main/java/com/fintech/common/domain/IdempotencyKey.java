package com.fintech.common.domain;

import lombok.Value;

import java.io.Serializable;
import java.util.UUID;

/**
 * Idempotency key for preventing duplicate transaction processing.
 * Should be unique per transaction request.
 */
@Value
public class IdempotencyKey implements Serializable {
    String value;

    public static IdempotencyKey generate() {
        return new IdempotencyKey(UUID.randomUUID().toString());
    }

    public static IdempotencyKey of(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Idempotency key cannot be null or blank");
        }
        return new IdempotencyKey(value);
    }
}



