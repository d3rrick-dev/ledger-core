package com.d3rrick.ledgercore.integration;

import com.d3rrick.ledgercore.repository.loan.LoanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
public abstract class IntegrationTestRoot extends PostgresSetup {

    //    @Autowired
    //    protected DSLContext dsl;

    @Autowired
    protected LoanRepository loanRepository;
}