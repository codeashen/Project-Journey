package com.ashen.ccfilm.consumer.feign;

import com.ashen.ccfilm.consumer.feign.fallback.MyFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Feign客户端接口
 */
@FeignClient(
        name = "hello-service-provider",   // 服务名，不指定url的时候默认是注册到Eureka上的服务名，所以指定服务名就可以天然集成Eureka
        // url = "localhost:7200",  // 手动指定url
        path = "/provider",         // 所有请求uri的前缀，相当于Controller上的RequestMapping
        // primary = false,         // 取消自定义实现类为第一优先级
        // fallback = ProviderFacadeFallbackImpl.class,    // 服务降级实现类，出错调用降级类中对应的方法
        fallbackFactory = MyFallbackFactory.class       // 服务降级实现类工厂
)
public interface ProviderFacade {

    // 测试发现这边不加 @RequestParam, Provider服务接收不到参数
    @RequestMapping(value = "/sayHello", method = RequestMethod.GET)
    String providerSayHello(@RequestParam("message") String message);
}
