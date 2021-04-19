package com.ashen.ccfilm.feignconf;

import feign.Contract;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * FeignClient注解 configuration属性所需要的配置
 */
@Configuration
public class FeignConfig {

    @Bean
    public Contract contract(){
        return new feign.Contract.Default();
    }
}
