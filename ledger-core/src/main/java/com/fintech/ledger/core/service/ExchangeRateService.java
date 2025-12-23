package com.fintech.ledger.core.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

@Service
public class ExchangeRateService {

    // Mock Rates
    private static final Map<String, BigDecimal> RATES = Map.of(
            "EUR_USD", new BigDecimal("1.10"),
            "USD_EUR", new BigDecimal("0.9090909091"));

    public BigDecimal getRate(String sourceCurrency, String targetCurrency) {
        if (sourceCurrency.equals(targetCurrency))
            return BigDecimal.ONE;

        String pair = sourceCurrency + "_" + targetCurrency;
        BigDecimal rate = RATES.get(pair);
        if (rate == null) {
            throw new IllegalArgumentException("No rate found for pair: " + pair);
        }
        return rate;
    }
}
