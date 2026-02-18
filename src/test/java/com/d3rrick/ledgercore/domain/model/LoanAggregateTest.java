package com.d3rrick.ledgercore.domain.model;

import com.d3rrick.ledgercore.domain.exception.DomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class LoanAggregateTest {
    private final UUID userId = UUID.randomUUID();
    private final Money thousand = new Money(new BigDecimal("1000.00"));

    @Test
    @DisplayName("Should transition from PENDING to ACTIVE via activate")
    void testActivation() {
        var loan = LoanAggregate.createNew(userId, thousand);
        var activated = loan.activate();

        assertThat(activated.status()).isEqualTo(LoanStatus.ACTIVE);
    }

    @Test
    @DisplayName("Should throw DomainException when activating a non-PENDING loan")
    void testInvalidActivation() {
        var loan = LoanAggregate.createNew(userId, thousand).activate();

        assertThrows(DomainException.class, loan::activate);
    }

    @Test
    @DisplayName("Should transition to CLOSED when balance hits zero exactly")
    void testRepaymentClosingLoan() {
        var loan = LoanAggregate.createNew(userId, thousand).activate();
        var payment = new Money(new BigDecimal("1000.00"));

        var closedLoan = loan.applyRepayment(payment);

        assertThat(closedLoan.status()).isEqualTo(LoanStatus.CLOSED);
        assertThat(closedLoan.currentBalance().amount()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Should throw DomainException for overpayment")
    void testOverpayment() {
        var loan = LoanAggregate.createNew(userId, thousand).activate();
        var tooMuch = new Money(new BigDecimal("1000.01"));

        var ex = assertThrows(DomainException.class, () -> loan.applyRepayment(tooMuch));
        assertThat(ex.getMessage()).contains("Repayment exceeds outstanding balance");
    }

    @Test
    @DisplayName("Should throw DomainException for negative repayment")
    void testNegativeRepayment() {
        var loan = LoanAggregate.createNew(userId, thousand).activate();
        var negative = new Money(new BigDecimal("-50.00"));

        assertThrows(DomainException.class, () -> loan.applyRepayment(negative));
    }

    @Test
    @DisplayName("Should throw exception when repayment is attempted on PENDING loan")
    void validateRepayment_PendingStatus_ThrowsException() {
        var loan = new LoanAggregate(
                UUID.randomUUID(),
                new Money(new BigDecimal("1000")),
                new Money(new BigDecimal("1000")),
                LoanStatus.PENDING,
                1L
        );

        assertThrows(DomainException.class, () ->
                loan.applyRepayment(new Money(new BigDecimal("100")))
        );
    }

    @Test
    @DisplayName("Should successfully subtract balance when applying repayment")
    void applyRepayment_ValidAmount_UpdatesBalance() {
        var loan = new LoanAggregate(
                UUID.randomUUID(),
                new Money(new BigDecimal("1000")),
                new Money(new BigDecimal("1000")),
                LoanStatus.ACTIVE,
                1L
        );
        var repayment = new Money(new BigDecimal("200"));
        var updatedLoan = loan.applyRepayment(repayment);

        assertThat(updatedLoan.currentBalance().amount()).isEqualByComparingTo("800.00");
        assertThat(updatedLoan.version()).isEqualTo(1L);
    }

    @Test
    @DisplayName("GIVEN a CLOSED loan WHEN a repayment is attempted THEN throw DomainException")
    void cannotPayClosedLoan() {
        var loan = new LoanAggregate(UUID.randomUUID(), new Money(new BigDecimal(100)), new Money(new BigDecimal(0)), LoanStatus.CLOSED, 1L);

        assertThrows(DomainException.class, () ->
                loan.applyRepayment(new Money(new BigDecimal("10")))
        );
    }

    @Test
    @DisplayName("GIVEN an ACTIVE loan WHEN balance reaches zero THEN status becomes CLOSED")
    void activeToClosedTransition() {
        var loan = new LoanAggregate(UUID.randomUUID(), new Money(new BigDecimal(100)), new Money(new BigDecimal(100)), LoanStatus.ACTIVE, 1L);
        var finalPayment = new Money(new BigDecimal("100"));

        var updated = loan.applyRepayment(finalPayment);

        assertThat(updated.status()).isEqualTo(LoanStatus.CLOSED);
        assertThat(updated.currentBalance().amount()).isEqualByComparingTo("0.00");
    }
}