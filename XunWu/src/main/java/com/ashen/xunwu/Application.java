package com.ashen.xunwu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication(
        // 关闭security的http基本验证
        exclude = {org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class}
)
@RestController
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @GetMapping("/hello")
    public String hello() {
        return "Hello, ashen";
    }
}
