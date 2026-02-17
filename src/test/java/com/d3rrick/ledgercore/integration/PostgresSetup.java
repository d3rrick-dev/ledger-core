package com.d3rrick.ledgercore.integration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

public abstract class PostgresSetup {
    private static final Logger log = LoggerFactory.getLogger(PostgresSetup.class);
    public static final PostgreSQLContainer POSTGRES_CONTAINER =
            new PostgreSQLContainer(DockerImageName.parse("postgres:16-alpine"))
                    .withLogConsumer(new Slf4jLogConsumer(log)) // adding db logs
                    .withDatabaseName("loan_db")
                    .withUsername("postgres")
                    .withPassword("secret");

    static {
        POSTGRES_CONTAINER.start();
    }

    @DynamicPropertySource
    static void overrideDatasourceProperties(DynamicPropertyRegistry registry) {

        registry.add("spring.datasource.url", POSTGRES_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES_CONTAINER::getUsername);
        registry.add("spring.datasource.password", POSTGRES_CONTAINER::getPassword);
        IO.println("**************" + POSTGRES_CONTAINER.getJdbcUrl());
    }
}