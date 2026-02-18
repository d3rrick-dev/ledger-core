package com.d3rrick.ledgercore.infrastructure.web.response;

import com.d3rrick.ledgercore.domain.model.LoanAggregate;

import java.math.BigDecimal;
import java.util.UUID;

public record LoanResponse(UUID userId, BigDecimal principalAmount, BigDecimal currentBalance, String status) {
    public static LoanResponse fromAggregate(LoanAggregate aggregate) {
        return new LoanResponse(
                aggregate.userId(),
                aggregate.principalAmount().amount(),
                aggregate.currentBalance().amount(),
                aggregate.status().name()
        );
    }
}