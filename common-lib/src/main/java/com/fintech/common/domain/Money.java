package com.fintech.common.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Objects;

@Getter
@EqualsAndHashCode
@ToString
public class Money implements Serializable {

    private static final int SCALE = 18;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_EVEN;

    private final BigDecimal amount;
    private final Currency currency;

    public Money(BigDecimal amount, Currency currency) {
        this.currency = Objects.requireNonNull(currency, "Currency must not be null");
        this.amount = Objects.requireNonNull(amount, "Amount must not be null")
                .setScale(SCALE, ROUNDING_MODE);
    }

    public static Money of(BigDecimal amount, String currencyCode) {
        return new Money(amount, Currency.getInstance(currencyCode));
    }

    public static Money zero(Currency currency) {
        return new Money(BigDecimal.ZERO, currency);
    }

    public Money add(Money other) {
        checkCurrency(other);
        return new Money(this.amount.add(other.amount), this.currency);
    }

    public Money subtract(Money other) {
        checkCurrency(other);
        return new Money(this.amount.subtract(other.amount), this.currency);
    }

    public boolean isPositive() {
        return this.amount.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean isNonNegative() {
        return this.amount.compareTo(BigDecimal.ZERO) >= 0;
    }

    private void checkCurrency(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("Currency mismatch: " + this.currency + " vs " + other.currency);
        }
    }

    /**
     * Protected no-args constructor for JPA providers.
     * Should not be used directly - use factory methods instead.
     */
    protected Money() {
        // JPA requires no-args constructor, but fields are set via setters
        // This is intentionally left with null values - JPA will populate them
        this.amount = null;
        this.currency = null;
    }
}
