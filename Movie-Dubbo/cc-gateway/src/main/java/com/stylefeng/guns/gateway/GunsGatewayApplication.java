package com.stylefeng.guns.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.cloud.netflix.hystrix.dashboard.EnableHystrixDashboard;

@SpringBootApplication(scanBasePackages = {"com.stylefeng.guns"})
// 以下三个注解用于开启Hystrix熔断器
@EnableHystrixDashboard
@EnableCircuitBreaker
@EnableHystrix
public class GunsGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GunsGatewayApplication.class, args);
    }
}
