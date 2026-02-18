package com.d3rrick.ledgercore.model;

import com.d3rrick.ledgercore.domain.model.LoanStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record Loan(
        UUID userId,
        BigDecimal amount,
        LoanStatus status
) {}