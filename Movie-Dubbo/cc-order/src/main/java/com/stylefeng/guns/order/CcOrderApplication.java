package com.stylefeng.guns.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.stylefeng.guns"})
public class CcOrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(CcOrderApplication.class, args);
    }
}
