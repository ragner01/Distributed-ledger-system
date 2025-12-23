package com.fintech.common.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;

import static org.junit.jupiter.api.Assertions.*;

class MoneyTest {

    private final Currency USD = Currency.getInstance("USD");
    private final Currency EUR = Currency.getInstance("EUR");

    @Test
    void testCreationAndPrecision() {
        Money money = new Money(new BigDecimal("100.1234567890123456789"), USD);
        assertEquals(new BigDecimal("100.123456789012345679"), money.getAmount()); // Rounding HALF_EVEN
        assertEquals(USD, money.getCurrency());
    }

    @Test
    void testAdditionSuccess() {
        Money m1 = new Money(new BigDecimal("10.00"), USD);
        Money m2 = new Money(new BigDecimal("20.00"), USD);
        Money sum = m1.add(m2);

        assertEquals(new BigDecimal("30.000000000000000000"), sum.getAmount());
    }

    @Test
    void testAdditionCurrencyMismatch() {
        Money m1 = new Money(BigDecimal.TEN, USD);
        Money m2 = new Money(BigDecimal.TEN, EUR);

        assertThrows(IllegalArgumentException.class, () -> m1.add(m2));
    }
}
