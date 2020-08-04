package com.shop.ClientServiceRest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class ClientServiceRestApp {
    public static void main(String[] args) {
        SpringApplication.run(ClientServiceRestApp.class, args);
    }
}
