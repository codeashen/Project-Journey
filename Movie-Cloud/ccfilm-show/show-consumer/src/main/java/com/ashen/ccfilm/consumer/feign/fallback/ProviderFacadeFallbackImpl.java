package com.ashen.ccfilm.consumer.feign.fallback;

import com.ashen.ccfilm.consumer.feign.ProviderFacade;
import org.springframework.stereotype.Service;

/**
 * FeignClient 降级实现，集成 Hystrix 使用
 */
@Service
public class ProviderFacadeFallbackImpl implements ProviderFacade {
    
    @Override
    public String providerSayHello(String message) {
        return "fallback method invoked.";
    }
}
