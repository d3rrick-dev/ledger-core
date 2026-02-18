package com.d3rrick.ledgercore.infrastructure.web;

import com.d3rrick.ledgercore.application.service.LoanService;
import com.d3rrick.ledgercore.domain.model.Money;
import com.d3rrick.ledgercore.infrastructure.web.request.LoanRequest;
import com.d3rrick.ledgercore.infrastructure.web.request.RepaymentRequest;
import com.d3rrick.ledgercore.infrastructure.web.response.LoanResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/loan")
@RequiredArgsConstructor
public class LoanController {

    private final LoanService loanService;

    @PostMapping("/{userId}/repayment")
    public ResponseEntity<Void> postRepayment(
            @PathVariable UUID userId,
            @RequestBody @Valid RepaymentRequest request) {

        // Map DTO to Domain Value Objects
        var repaymentAmount = new Money(request.amount());

        // Delegate to Application Service
        loanService.processRepayment(userId, repaymentAmount, request.idempotencyKey());

        return ResponseEntity.accepted().build();
    }

    @GetMapping("/{userId}")
    public ResponseEntity<LoanResponse> getLoan(@PathVariable UUID userId) {
        return loanService.getLoanDetails(userId)
                .map(LoanResponse::fromAggregate)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Void> createLoan(@RequestBody @Valid LoanRequest request) {
        loanService.originateLoan(
                UUID.fromString(request.userId()),
                new Money(request.amount()),
                UUID.fromString(request.idempotencyKey())
        );
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}