package com.ashen.ccfilm.film;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

@EnableDiscoveryClient
@ComponentScan("com.ashen.ccfilm")
@MapperScan("com.ashen.ccfilm.film.dao")
@SpringBootApplication
public class FilmApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(FilmApplication.class, args);
    }
    
}
