package com.d3rrick.ledgercore.application.service;

import com.d3rrick.ledgercore.application.port.LedgerRepository;
import com.d3rrick.ledgercore.domain.exception.EntityNotFoundException;
import com.d3rrick.ledgercore.domain.model.LoanStatus;
import com.d3rrick.ledgercore.domain.model.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoanServiceTest {

    @Mock
    private LedgerRepository ledgerRepository;

    @InjectMocks
    private LoanService loanService;

    @Test
    @DisplayName("Should successfully originate and activate a loan")
    void testOriginateLoan() {
        var userId = UUID.randomUUID();
        var key = UUID.randomUUID();
        var amount = new Money(new BigDecimal("500.00"));

        loanService.originateLoan(userId, amount, key);

        verify(ledgerRepository).createInitialLoan(
                argThat(l -> l.status() == LoanStatus.ACTIVE && l.userId().equals(userId)),
                eq(key)
        );
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when loan does not exist")
    void testRepaymentLoanNotFound() {
        var userId = UUID.randomUUID();
        when(ledgerRepository.findByUserId(userId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () ->
                loanService.processRepayment(userId, new Money(BigDecimal.TEN), UUID.randomUUID())
        );
    }
}