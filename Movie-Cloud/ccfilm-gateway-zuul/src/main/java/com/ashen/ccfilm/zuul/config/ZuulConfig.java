package com.ashen.ccfilm.zuul.config;

import com.ashen.ccfilm.zuul.filter.JWTFilter;
import com.ashen.ccfilm.zuul.filter.MyFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ZuulConfig {
    
    // 自定义拦截器
    @Bean
    public MyFilter myFilter() {
        return new MyFilter();
    }
    
    // JWT拦截器
    @Bean
    public JWTFilter jwtFilter() {
        return new JWTFilter();
    }
    
}
