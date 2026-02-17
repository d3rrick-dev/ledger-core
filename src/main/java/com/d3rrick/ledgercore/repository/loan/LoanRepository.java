package com.d3rrick.ledgercore.repository.loan;

import com.d3rrick.ledgercore.model.Loan;
import com.d3rrick.ledgercore.model.LoanStatus;

import java.util.List;

public interface LoanRepository {
    Loan save(Loan loan);
    List<Loan> getLoanByStatus(LoanStatus status);
    Loan getLoanByUserId(String userId);
}
