package com.stylefeng.guns.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.stylefeng.guns"})
public class GunsUserApplication {

    public static void main(String[] args) {
        SpringApplication.run(GunsUserApplication.class, args);
    }
}
