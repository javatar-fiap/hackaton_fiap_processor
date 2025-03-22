package com.fiap.processor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.fiap.processor")
public class ProcessorApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProcessorApplication.class, args);
    }
}
