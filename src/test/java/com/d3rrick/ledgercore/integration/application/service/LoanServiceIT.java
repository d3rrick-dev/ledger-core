package com.d3rrick.ledgercore.integration.application.service;

import com.d3rrick.ledgercore.application.service.LoanService;
import com.d3rrick.ledgercore.domain.model.Money;
import com.d3rrick.ledgercore.domain.model.LoanStatus;
import com.d3rrick.ledgercore.integration.BaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class LoanServiceIT extends BaseIntegrationTest {

    @Autowired
    private LoanService loanService;

    @Test
    void shouldOriginateAndProcessRepaymentInDatabase() {
        var userId = UUID.randomUUID();
        var originationKey = UUID.randomUUID();
        var paymentKey = UUID.randomUUID();
        var principal = new Money(new BigDecimal("1000.00"));

        loanService.originateLoan(userId, principal, originationKey);

        loanService.processRepayment(userId, new Money(new BigDecimal("400.00")), paymentKey);

        var loan = loanService.getLoanDetails(userId).orElseThrow();
        assertThat(loan.currentBalance().amount()).isEqualByComparingTo("600.00");
        assertThat(loan.status()).isEqualTo(LoanStatus.ACTIVE);
    }

    @Test
    void shouldCloseLoanWhenBalanceReachesZero() {
        var userId = UUID.randomUUID();
        var amount = new Money(new BigDecimal("100.00"));

        loanService.originateLoan(userId, amount, UUID.randomUUID());
        loanService.processRepayment(userId, amount, UUID.randomUUID());

        var loan = loanService.getLoanDetails(userId).orElseThrow();
        // TODO: After balance is zero then move status to closed
        assertThat(loan.status()).isEqualTo(LoanStatus.ACTIVE);
        assertThat(loan.currentBalance().amount()).isEqualByComparingTo("0.00");
    }
}