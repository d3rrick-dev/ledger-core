package com.d3rrick.ledgercore.infrastructure.persistence;

import com.d3rrick.ledgercore.application.port.LedgerRepository;
import com.d3rrick.ledgercore.domain.model.LoanAggregate;
import com.d3rrick.ledgercore.domain.model.LoanStatus;
import com.d3rrick.ledgercore.domain.model.Money;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.ConcurrentModificationException;
import java.util.Optional;
import java.util.UUID;

import static com.d3rrick.ledgercore.infrastructure.jooq.Tables.LEDGER_ENTRY;
import static com.d3rrick.ledgercore.infrastructure.jooq.Tables.LOAN;

@Repository
@RequiredArgsConstructor
class JooqLedgerRepository implements LedgerRepository {

    private final DSLContext dsl;

    @Override
    public void recordTransaction(LoanAggregate loan, Money delta, String type, UUID idempotencyKey) {
        // All database work happens inside a single ACID transaction
        dsl.transaction(configuration -> {
            var ctx = configuration.dsl();

            // 1. Attempt to insert the Ledger Entry first
            // If the idempotencyKey already exists, this throws a DataAccessException
            ctx.insertInto(LEDGER_ENTRY)
                    .set(LEDGER_ENTRY.USER_ID, loan.userId())
                    .set(LEDGER_ENTRY.AMOUNT_DELTA, delta.amount())
                    .set(LEDGER_ENTRY.ENTRY_TYPE, type)
                    .set(LEDGER_ENTRY.IDEMPOTENCY_KEY, idempotencyKey)
                    .execute();

            // 2. Update the Snapshot with Optimistic Locking
            // We only update if the version in DB matches the version in our Java Object
            int updatedRows = ctx.update(LOAN)
                    .set(LOAN.CURRENT_BALANCE, loan.currentBalance().amount())
                    .set(LOAN.VERSION, loan.version() + 1) // Increment version
                    .set(LOAN.UPDATED_AT, OffsetDateTime.now())
                    .where(LOAN.USER_ID.eq(loan.userId()))
                    .and(LOAN.VERSION.eq(loan.version()))
                    .execute();

            // 3. Safety Check
            if (updatedRows == 0) {
                // This means another thread changed the record between our Read and Write
                throw new ConcurrentModificationException(
                        "Loan state was modified by another process. Please retry."
                );
            }
        });
    }

    @Override
    public Optional<LoanAggregate> findByUserId(UUID userId) {
        return dsl.selectFrom(LOAN)
                .where(LOAN.USER_ID.eq(userId))
                .fetchOptional()
                .map(r -> new LoanAggregate(
                        r.getUserId(),
                        new Money(r.getPrincipalAmount()),
                        new Money(r.getCurrentBalance()),
                        LoanStatus.valueOf(r.getStatus()),
                        r.getVersion()
                ));
    }

    @Override
    public void createInitialLoan(LoanAggregate loan, UUID idempotencyKey) {
        dsl.transaction(configuration -> {
            var ctx = configuration.dsl();

            // 1. Insert the Snapshot
            ctx.insertInto(LOAN)
                    .set(LOAN.USER_ID, loan.userId())
                    .set(LOAN.PRINCIPAL_AMOUNT, loan.principalAmount().amount())
                    .set(LOAN.CURRENT_BALANCE, loan.currentBalance().amount())
                    .set(LOAN.STATUS, loan.status().name())
                    .set(LOAN.VERSION, 1L)
                    .execute();

            // 2. Insert the First Ledger Entry (The "Disbursement")
            ctx.insertInto(LEDGER_ENTRY)
                    .set(LEDGER_ENTRY.USER_ID, loan.userId())
                    .set(LEDGER_ENTRY.AMOUNT_DELTA, loan.principalAmount().amount())
                    .set(LEDGER_ENTRY.ENTRY_TYPE, "DISBURSEMENT")
                    .set(LEDGER_ENTRY.IDEMPOTENCY_KEY, idempotencyKey)
                    .execute();
        });
    }

}