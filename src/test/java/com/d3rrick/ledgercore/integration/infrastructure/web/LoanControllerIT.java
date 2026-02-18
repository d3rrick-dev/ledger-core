package com.d3rrick.ledgercore.integration.infrastructure.web;

import com.d3rrick.ledgercore.infrastructure.web.request.LoanRequest;
import com.d3rrick.ledgercore.infrastructure.web.request.RepaymentRequest;
import com.d3rrick.ledgercore.infrastructure.web.response.LoanResponse;
import com.d3rrick.ledgercore.integration.BaseIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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

    @Test
    @DisplayName("Concurrency Test: 10 parallel repayments should result in 0 balance")
    void concurrentRepayments_ShouldMaintainIntegrity() throws InterruptedException {
        var userId = UUID.randomUUID().toString();
        var totalAmount = new BigDecimal("100.00");
        var paymentAmount = new BigDecimal("10.00");
        int numberOfThreads = 10;

        webClient.post()
                .uri("/api/v1/loan")
                .bodyValue(new LoanRequest(userId, totalAmount, UUID.randomUUID().toString()))
                .exchange()
                .expectStatus().isCreated();

        var executor = Executors.newFixedThreadPool(numberOfThreads);
        var latch = new CountDownLatch(1);
        var doneLatch = new CountDownLatch(numberOfThreads);

        for (int i = 0; i < numberOfThreads; i++) {
            executor.submit(() -> {
                try {
                    latch.await();

                    webClient.post()
                            .uri("/api/v1/loan/{userId}/repayment", userId)
                            .bodyValue(new RepaymentRequest(paymentAmount, UUID.randomUUID()))
                            .exchange();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        latch.countDown();
        var _ = doneLatch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        webClient.get()
                .uri("/api/v1/loan/{userId}", userId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(LoanResponse.class)
                .value(response -> {
                    assertThat(response.currentBalance())
                            .as("Balance should be zero after 10 payments of 10.00")
                            .isEqualByComparingTo(BigDecimal.ZERO);
                });
    }
}