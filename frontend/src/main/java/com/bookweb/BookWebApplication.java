package com.bookweb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BookWebApplication {
    public static void main(String[] args) {
        SpringApplication.run(BookWebApplication.class, args);
        System.setProperty("spring.thymeleaf.cache", "false");
    }
}
