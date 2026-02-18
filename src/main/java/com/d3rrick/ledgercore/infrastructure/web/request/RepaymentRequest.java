package com.d3rrick.ledgercore.infrastructure.web.request;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record RepaymentRequest(
        @NotNull BigDecimal amount,
        @NotNull UUID idempotencyKey // Can also be sent via X-Idempotency-Key header
) {}