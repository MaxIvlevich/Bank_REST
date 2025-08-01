package com.example.bankcards;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer"
)
@SpringBootApplication
public class BankCardsApi {
    public static void main(String[] args) {
        SpringApplication.run(BankCardsApi.class, args);
    }
}
