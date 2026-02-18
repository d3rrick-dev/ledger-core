package com.d3rrick.ledgercore.infrastructure.web.exceptions;

import com.d3rrick.ledgercore.domain.exception.DomainException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.net.URI;
import java.util.ConcurrentModificationException;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    // 1. Handle Business Rule Violations (e.g., "Loan is not active")
    @ExceptionHandler(DomainException.class)
    public ProblemDetail handleDomainException(DomainException ex) {
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problem.setTitle("Business Rule Violation");
        problem.setType(URI.create("https://ledger-core.com/errors/business-rule"));
        return problem;
    }

    // 2. Handle Optimistic Locking Failures (Concurrency)
    @ExceptionHandler(ConcurrentModificationException.class)
    public ProblemDetail handleConcurrencyException(ConcurrentModificationException ex) {
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, "The record was updated by another request. Please retry.");
        problem.setTitle("Edit Conflict");
        return problem;
    }

    // 3. Handle Idempotency/Database Violations (e.g., Unique Constraint)
    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    public ProblemDetail handleDataIntegrity(org.springframework.dao.DataIntegrityViolationException ex) {
        // We assume it's an idempotency failure if it's a unique constraint on ledger_entry
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, "Duplicate transaction detected (Idempotency Key violation).");
        problem.setTitle("Duplicate Request");
        return problem;
    }

    // 4. Fallback for everything else
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGenericException(Exception ex) {
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred.");
        problem.setTitle("Server Error");
        return problem;
    }
}