package com.d3rrick.ledgercore.application.port;

import com.d3rrick.ledgercore.domain.model.LoanAggregate;
import com.d3rrick.ledgercore.domain.model.Money;

import java.util.Optional;
import java.util.UUID;

public interface LedgerRepository {
    void recordTransaction(LoanAggregate loan, Money delta, String type, UUID idempotencyKey);

    Optional<LoanAggregate> findByUserId(UUID userId);

    void createInitialLoan(LoanAggregate loan, UUID idempotencyKey);
}
