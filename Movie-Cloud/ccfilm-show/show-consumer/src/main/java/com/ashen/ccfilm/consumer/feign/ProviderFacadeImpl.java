package com.ashen.ccfilm.consumer.feign;

import org.springframework.stereotype.Service;

/**
 * FeignClient 自定义实现，不行使用代理自动生成的现实，可以用自定义实现
 * 使用步骤：
 *      1. FeignClient注解属性 primary=false
 *      2. 编写自定义实现，加上 @Primary 注解
 */
// @Primary
@Service
public class ProviderFacadeImpl implements ProviderFacade {
    
    @Override
    public String providerSayHello(String message) {
        return "自定义实现返回";
    }
}
