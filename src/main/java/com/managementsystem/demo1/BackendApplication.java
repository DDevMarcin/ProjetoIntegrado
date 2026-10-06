package com.managementsystem.demo1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BackendApplication {

    // Roda a API REST sozinha (sem JavaFX), em: mvn spring-boot:run
    // http://localhost:8080/produtos
    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }
}