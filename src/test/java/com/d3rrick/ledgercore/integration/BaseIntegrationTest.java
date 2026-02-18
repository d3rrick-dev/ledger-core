package com.d3rrick.ledgercore.integration;

import com.d3rrick.ledgercore.repository.loan.LoanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
@AutoConfigureWebTestClient
public abstract class BaseIntegrationTest extends PostgresSetup {

    //    @Autowired
    //    protected DSLContext dsl;

    @Autowired
    protected LoanRepository loanRepository;

    @Autowired
    protected WebTestClient webClient;
}