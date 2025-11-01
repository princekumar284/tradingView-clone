package com.tradingapp;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication   //@SpringBootApplication and contains the main() method, which serves as the entry point for the application.
public class TradingBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(TradingBackendApplication.class, args);
    }
}
