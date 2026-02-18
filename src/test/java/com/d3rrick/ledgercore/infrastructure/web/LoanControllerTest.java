package com.d3rrick.ledgercore.infrastructure.web;

import com.d3rrick.ledgercore.application.service.LoanService;
import com.d3rrick.ledgercore.domain.model.LoanAggregate;
import com.d3rrick.ledgercore.domain.model.LoanStatus;
import com.d3rrick.ledgercore.domain.model.Money;
import com.d3rrick.ledgercore.infrastructure.web.request.LoanRequest;
import com.d3rrick.ledgercore.infrastructure.web.request.RepaymentRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Web Infrastructure only start (Tomcat, Jackson for JSON, and the Controller)
@WebMvcTest(LoanController.class)
class LoanControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LoanService loanService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("POST /repayment - Should return 202 Accepted when valid")
    void postRepayment_Success() throws Exception {
        var userId = UUID.randomUUID();
        var request = new RepaymentRequest(new BigDecimal("100.00"), UUID.randomUUID());

        mockMvc.perform(post("/api/v1/loan/{userId}/repayment", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted());

        verify(loanService).processRepayment(eq(userId), any(Money.class), eq(request.idempotencyKey()));
    }

    @Test
    @DisplayName("GET /{userId} - Should return 200 and data when loan exists")
    void getLoan_Found() throws Exception {
        var userId = UUID.randomUUID();
        var loan = new LoanAggregate(userId, new Money(new BigDecimal("1000")), new Money(new BigDecimal("500")), LoanStatus.ACTIVE, 1L);

        when(loanService.getLoanDetails(userId)).thenReturn(Optional.of(loan));

        mockMvc.perform(get("/api/v1/loan/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.currentBalance").value(500.00))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @DisplayName("GET /{userId} - Should return 404 when loan missing")
    void getLoan_NotFound() throws Exception {
        var userId = UUID.randomUUID();
        when(loanService.getLoanDetails(userId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/loan/{userId}", userId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST / - Should return 201 Created for valid loan origination")
    void createLoan_Success() throws Exception {
        var userId = UUID.randomUUID();
        var idempKey = UUID.randomUUID();
        var request = new LoanRequest(userId.toString(), new BigDecimal("5000.00"), idempKey.toString());

        mockMvc.perform(post("/api/v1/loan")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        verify(loanService).originateLoan(eq(userId), any(Money.class), eq(idempKey));
    }

    @Test
    @DisplayName("POST / - Should return 400 Bad Request when validation fails (null amount)")
    void createLoan_ValidationError() throws Exception {
        var request = new LoanRequest(UUID.randomUUID().toString(), null, UUID.randomUUID().toString());

        mockMvc.perform(post("/api/v1/loan")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}