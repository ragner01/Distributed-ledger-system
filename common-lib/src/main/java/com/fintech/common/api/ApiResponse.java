package com.fintech.common.api;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
public class ApiResponse<T> {

    private final String traceId;
    private final Instant timestamp;
    private final T data;
    private final String errorDetails;

    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .traceId(UUID.randomUUID().toString())
                .timestamp(Instant.now())
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> error(String errorDetails, String traceId) {
        return ApiResponse.<T>builder()
                .traceId(traceId != null ? traceId : UUID.randomUUID().toString())
                .timestamp(Instant.now())
                .errorDetails(errorDetails)
                .build();
    }
}
