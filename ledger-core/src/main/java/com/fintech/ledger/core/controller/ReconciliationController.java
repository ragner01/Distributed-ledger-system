package com.fintech.ledger.core.controller;

import com.fintech.common.api.ApiResponse;
import com.fintech.ledger.core.jobs.ReconciliationJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/reconciliation")
@RequiredArgsConstructor
@Slf4j
public class ReconciliationController {

    private final ReconciliationJob reconciliationJob;

    @PostMapping("/run")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, String>>> runReconciliation() {
        log.info("Manual reconciliation triggered via API");
        
        try {
            reconciliationJob.reconcile();
            return ResponseEntity.ok(ApiResponse.success(
                Map.of("status", "SUCCESS", "message", "Reconciliation completed successfully")));
        } catch (Exception e) {
            log.error("Reconciliation failed: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(ApiResponse.error(
                "Reconciliation failed: " + e.getMessage(), null));
        }
    }

    @GetMapping("/status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStatus() {
        boolean isHalted = ReconciliationJob.isSystemHalted();
        return ResponseEntity.ok(ApiResponse.success(
            Map.of("systemHalted", isHalted, 
                   "status", isHalted ? "HALTED" : "OPERATIONAL")));
    }
}


