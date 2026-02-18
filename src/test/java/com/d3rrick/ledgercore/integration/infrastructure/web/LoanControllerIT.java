package com.d3rrick.ledgercore.integration.infrastructure.web;

import com.d3rrick.ledgercore.infrastructure.web.request.LoanRequest;
import com.d3rrick.ledgercore.infrastructure.web.request.RepaymentRequest;
import com.d3rrick.ledgercore.infrastructure.web.response.LoanResponse;
import com.d3rrick.ledgercore.integration.BaseIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

import org.springframework.http.MediaType;

class LoanControllerIT extends BaseIntegrationTest {

    @Test
    @DisplayName("Full Flow: Create Loan via API - Repay via API - Verify State")
    void fullLoanLifecycleIntegrationTest() {
        var userId = UUID.randomUUID().toString();
        var idempKey = UUID.randomUUID().toString();
        var createRequest = new LoanRequest(userId, new BigDecimal("1000.00"), idempKey);

        webClient.post()
                .uri("/api/v1/loan")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(createRequest)
                .exchange()
                .expectStatus().isCreated();

        var repaymentRequest = new RepaymentRequest(new BigDecimal("250.00"), UUID.randomUUID());

        webClient.post()
                .uri("/api/v1/loan/{userId}/repayment", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(repaymentRequest)
                .exchange()
                .expectStatus().isAccepted();

        webClient.get()
                .uri("/api/v1/loan/{userId}", userId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(LoanResponse.class)
                .value(response -> {
                    assertThat(response.userId().toString()).isEqualTo(userId);
                    assertThat(response.currentBalance()).isEqualByComparingTo("750.00");
                    assertThat(response.status()).isEqualTo("ACTIVE");
                });
    }

    @Test
    @DisplayName("Should return 404 when requesting non-existent loan")
    void getLoan_NotFound() {
        webClient.get()
                .uri("/api/v1/loan/{userId}", UUID.randomUUID())
                .exchange()
                .expectStatus().isNotFound();
    }
}