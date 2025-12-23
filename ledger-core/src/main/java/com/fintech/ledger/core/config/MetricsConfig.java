package com.fintech.ledger.core.config;

import com.fintech.ledger.core.metrics.LedgerMetrics;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class MetricsConfig {

    private final LedgerMetrics ledgerMetrics;

    @Bean
    public CommandLineRunner initializeMetrics() {
        return args -> ledgerMetrics.initialize();
    }
}



