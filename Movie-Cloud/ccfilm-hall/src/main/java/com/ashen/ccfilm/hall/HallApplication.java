package com.ashen.ccfilm.hall;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@EnableFeignClients
@EnableDiscoveryClient
@ComponentScan("com.ashen.ccfilm")
@MapperScan("com.ashen.ccfilm.hall.dao")
@SpringBootApplication
public class HallApplication {

    public static void main(String[] args) {
        SpringApplication.run(HallApplication.class, args);
    }

}