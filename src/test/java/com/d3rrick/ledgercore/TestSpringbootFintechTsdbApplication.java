package com.d3rrick.ledgercore;

import org.springframework.boot.SpringApplication;


// running the app locally
public class TestSpringbootFintechTsdbApplication {

    static void main(String[] args) {
        SpringApplication.from(SpringbootFintechTsdbApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
