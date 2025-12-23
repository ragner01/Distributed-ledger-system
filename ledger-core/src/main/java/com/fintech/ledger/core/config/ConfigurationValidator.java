package com.fintech.ledger.core.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Validates critical configuration at startup.
 */
@Component
@Order(1) // Run early
@Slf4j
public class ConfigurationValidator implements CommandLineRunner {

    @Value("${spring.datasource.url:}")
    private String datasourceUrl;

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri:}")
    private String oauth2IssuerUri;

    @Override
    public void run(String... args) {
        log.info("Validating configuration...");

        boolean hasErrors = false;

        // Validate database configuration
        if (datasourceUrl == null || datasourceUrl.isBlank()) {
            log.error("CRITICAL: Database URL is not configured!");
            hasErrors = true;
        } else if (datasourceUrl.contains("localhost") && !datasourceUrl.contains("test")) {
            log.warn("WARNING: Database URL points to localhost - ensure this is intentional for production");
        }

        // Validate OAuth2 configuration
        if (oauth2IssuerUri == null || oauth2IssuerUri.isBlank()) {
            log.error("CRITICAL: OAuth2 issuer URI is not configured!");
            hasErrors = true;
        } else if (oauth2IssuerUri.contains("localhost")) {
            log.warn("WARNING: OAuth2 issuer URI points to localhost - ensure this is intentional for production");
        }

        // Validate Redis configuration (if used)
        // Add more validations as needed

        if (hasErrors) {
            log.error("Configuration validation failed! Application may not function correctly.");
            throw new IllegalStateException("Critical configuration validation failed");
        }

        log.info("Configuration validation passed");
    }
}



