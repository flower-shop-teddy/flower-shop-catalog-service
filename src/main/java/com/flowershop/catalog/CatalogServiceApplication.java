package com.flowershop.catalog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Quét thêm package common để nhận GlobalExceptionHandler và CorrelationIdFilter từ common-lib
@SpringBootApplication(scanBasePackages = {"com.flowershop.catalog", "com.flowershop.common"})
public class CatalogServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(CatalogServiceApplication.class, args);
    }
}
