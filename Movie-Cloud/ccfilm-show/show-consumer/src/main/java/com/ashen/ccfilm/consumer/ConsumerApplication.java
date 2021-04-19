package com.ashen.ccfilm.consumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients     // 开启Feign支持
@EnableDiscoveryClient  // DiscoverClient可以集成大部分注册中心
// @EnableEurekaClient  // 只对Eureka使用
@SpringBootApplication
public class ConsumerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConsumerApplication.class, args);
    }

}
