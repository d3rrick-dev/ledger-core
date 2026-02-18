package com.d3rrick.ledgercore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableRetry
public class SpringbootFintechTsdbApplication {
    public static void main(String[] args) {
        SpringApplication.run(SpringbootFintechTsdbApplication.class, args);
    }
}
