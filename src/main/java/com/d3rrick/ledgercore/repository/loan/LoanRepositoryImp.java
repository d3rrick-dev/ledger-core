package com.d3rrick.ledgercore.repository.loan;

import com.d3rrick.ledgercore.model.Loan;
import com.d3rrick.ledgercore.model.LoanStatus;
import com.d3rrick.ledgercore.repository.jooq.tables.records.LoanRecord;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

import static com.d3rrick.ledgercore.repository.jooq.Tables.LOAN;


@Repository
public class LoanRepositoryImp implements LoanRepository {
    private final DSLContext dsl;

    public LoanRepositoryImp(DSLContext dsl) {
        this.dsl = dsl;
    }

    public Loan save(Loan loan) {
        return dsl.insertInto(LOAN)
                .set(LOAN.USER_ID, loan.userId())
                .set(LOAN.AMOUNT, loan.amount())
                .set(LOAN.STATUS, loan.status().name())
                .returning()
                .fetchOne(this::map);
    }

    public List<Loan> getLoanByStatus(LoanStatus status) {
        return dsl.selectFrom(LOAN)
                .where(LOAN.STATUS.eq(status.name()))
                .fetch(this::map);
    }

    @Override
    public Loan getLoanByUserId(String userId) {
        return dsl.selectFrom(LOAN)
                .where(LOAN.USER_ID.eq(UUID.fromString(userId)))
                .fetchOne(this::map);
    }

    private Loan map(LoanRecord record) {
         return new Loan(
                record.getUserId(),
                record.getAmount(),
                LoanStatus.valueOf(record.getStatus())
        );
    }
}