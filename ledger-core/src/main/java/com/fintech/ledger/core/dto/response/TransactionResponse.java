package com.fintech.ledger.core.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class TransactionResponse {
    private Long journalEntryId;
    private String description;
    private Instant timestamp;
    private String idempotencyKey;
}



