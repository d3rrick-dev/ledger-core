package com.d3rrick.ledgercore.domain.model;

import com.d3rrick.ledgercore.domain.exception.DomainException;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * LoanAggregate acts as the State Machine and Consistency Boundary for the Ledger.
 */
public record LoanAggregate(
        UUID userId,
        Money principalAmount,
        Money currentBalance,
        LoanStatus status,
        long version) {

    /**
     * Factory method for initial creation.
     */
    public static LoanAggregate createNew(UUID userId, Money amount) {
        return new LoanAggregate(userId, amount, amount, LoanStatus.PENDING, 1L);
    }

    /**
     * Transition: PENDING -> ACTIVE
     * Represents the moment funds are disbursed.
     */
    public LoanAggregate activate() {
        ensureStatus(LoanStatus.PENDING, "activate");
        return new LoanAggregate(userId, principalAmount, currentBalance, LoanStatus.ACTIVE, version);
    }

    /**
     * Transition: ACTIVE/DEFAULTED -> CLOSED (if balance is 0)
     * Handles the reduction of debt.
     */
    public LoanAggregate applyRepayment(Money repayment) {
        if (this.status != LoanStatus.ACTIVE && this.status != LoanStatus.DEFAULTED) {
            throw new DomainException("Loan cannot accept payments in status: " + status);
        }

        validateRepaymentAmount(repayment);

        var newBalance = this.currentBalance.subtract(repayment);

        // State Machine Rule: Balance of zero forces CLOSED status
        var nextStatus = (newBalance.amount().compareTo(BigDecimal.ZERO) == 0)
                ? LoanStatus.CLOSED
                : this.status;

        return new LoanAggregate(userId, principalAmount, newBalance, nextStatus, version);
    }

    /**
     * Transition: ACTIVE -> DEFAULTED
     */
    public LoanAggregate markAsDefaulted() {
        ensureStatus(LoanStatus.ACTIVE, "default");
        return new LoanAggregate(userId, principalAmount, currentBalance, LoanStatus.DEFAULTED, version);
    }

    // Helper Methods
    private void ensureStatus(LoanStatus expected, String action) {
        if (this.status != expected) {
            throw new DomainException("Cannot " + action + " loan in status: " + status);
        }
    }

    private void validateRepaymentAmount(Money amount) {
        if (amount.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new DomainException("Repayment amount must be positive");
        }
        if (amount.amount().compareTo(this.currentBalance.amount()) > 0) {
            throw new DomainException("Repayment exceeds outstanding balance of " + currentBalance.amount());
        }
    }
}