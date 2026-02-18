package com.d3rrick.ledgercore.domain.model;

import jakarta.annotation.Nonnull;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record Money(@Nonnull BigDecimal amount) {
    public Money {
        // Standardize to 2 decimal places
        amount = amount.setScale(2, RoundingMode.HALF_UP);
    }

    public Money subtract(Money other) {
        return new Money(this.amount().subtract(other.amount()));
    }

    public Money negate() {
        return new Money(this.amount.negate());
    }
}
