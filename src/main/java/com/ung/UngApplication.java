package com.ung;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Main Application Class
 * QueryDSL and SQL Data Synchronization Application
 */
@SpringBootApplication
@EnableJpaRepositories
public class UngApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(UngApplication.class, args);
    }
}
