package com.d3rrick.ledgercore.application.service;

import com.d3rrick.ledgercore.application.port.LedgerRepository;
import com.d3rrick.ledgercore.domain.exception.EntityNotFoundException;
import com.d3rrick.ledgercore.domain.model.LoanAggregate;
import com.d3rrick.ledgercore.domain.model.Money;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LoanService {

    private final LedgerRepository ledgerRepository;

    @Transactional
    public void processRepayment(UUID userId, Money amount, UUID idempotencyKey) {
        var loan = ledgerRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("Loan not found: " + userId));

        var updatedLoan = loan.applyRepayment(amount);

        ledgerRepository.recordTransaction(
                updatedLoan,
                amount.negate(),
                "REPAYMENT",
                idempotencyKey
        );
    }

    @Transactional
    public void originateLoan(UUID userId, Money amount, UUID idempotencyKey) {
        // manual activation but there could be checks/bs rules before activation
        var newLoan = LoanAggregate.createNew(userId, amount).activate();

        ledgerRepository.createInitialLoan(newLoan, idempotencyKey);
    }

    @Transactional(readOnly = true)
    public Optional<LoanAggregate> getLoanDetails(UUID userId) {
        return ledgerRepository.findByUserId(userId);
    }
}