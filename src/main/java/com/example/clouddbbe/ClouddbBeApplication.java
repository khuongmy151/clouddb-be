package com.example.clouddbbe;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;


@EnableAsync // <-- Bắt buộc phải có để @Async hoạt động
@SpringBootApplication
public class ClouddbBeApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClouddbBeApplication.class, args);
    }


}
