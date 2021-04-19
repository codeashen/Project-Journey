package com.ashen.ccfilm.consumer.feign.fallback;

import com.ashen.ccfilm.consumer.feign.ProviderFacade;
import feign.hystrix.FallbackFactory;
import org.springframework.stereotype.Service;

/**
 * Fallback实现类工厂，用来获取FeignClient的Fallback实现类
 */
@Service
public class MyFallbackFactory implements FallbackFactory<ProviderFacade> {

    /**
     * 既可以捕获到参数，也可以捕获到异常
     * @param throwable 捕获到的异常
     * @return Fallback实现类
     */
    @Override
    public ProviderFacade create(Throwable throwable) {
        System.err.println("进入 Fallback工厂的 create方法");
        
        return new ProviderFacade() {
            @Override
            public String providerSayHello(String message) {
                return "Fallback工厂创建的实现类返回值, message: " + message + ", exception: " + throwable.getMessage();
            }
        };
    }
    
}
