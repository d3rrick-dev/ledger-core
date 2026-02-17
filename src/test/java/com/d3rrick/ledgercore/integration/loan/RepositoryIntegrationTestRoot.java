package com.d3rrick.ledgercore.integration.loan;

import com.d3rrick.ledgercore.integration.IntegrationTestRoot;
import com.d3rrick.ledgercore.model.Loan;
import com.d3rrick.ledgercore.model.LoanStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class RepositoryIntegrationTest extends IntegrationTestRoot {

    @Test
    void createLoan_shouldPersistToDatabaseAndReturnLoan() {
        var userId = UUID.randomUUID();
        var loan = new Loan(userId, new BigDecimal("100.00"), LoanStatus.PENDING);
        loanRepository.save(loan);
        var saved = loanRepository.getLoanByUserId(loan.userId().toString());
        assertThat(saved.userId()).isEqualTo(loan.userId());
    }

    @Test
    void getLoanById_shouldReturnLoan() {
        var userId = UUID.randomUUID();
        var loan = new Loan(userId, new BigDecimal("200.00"), LoanStatus.PENDING);
        loanRepository.save(loan);
        var createdLoan = loanRepository.getLoanByStatus(LoanStatus.PENDING).getFirst();
        assertThat(loan).isEqualTo(createdLoan);
    }

    @Test
    void createLoan_shouldPersistToDatabases() {
        var userId = UUID.randomUUID();
        var loan = new Loan(userId, new BigDecimal("300.00"), LoanStatus.PENDING);
        loanRepository.save(loan);
        var saved = loanRepository.getLoanByUserId(loan.userId().toString());
        assertThat(saved.userId()).isEqualTo(loan.userId());
    }
}