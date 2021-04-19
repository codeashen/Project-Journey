package com.ashen.ccfilm.consumer.controller;

import com.ashen.ccfilm.consumer.feign.ProviderFacade;
import com.ashen.ccfilm.consumer.service.ConsumerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@Slf4j
@RestController
public class ConsumerController {
    
    @Autowired
    private ConsumerService consumerService;

    // 注入Feign客户端
    @Resource
    private ProviderFacade providerFacade;

    /**
     * 直接调用
     */
    @RequestMapping(value = "/sayHello1")
    public String sayHello1(String message){
        return consumerService.callProvider1(message);
    }

    /**
     * LoadBalancerClient调用
     */
    @RequestMapping(value = "/sayHello2")
    public String sayHello2(String message){
        return consumerService.callProvider2(message);
    }

    /**
     * 负载均衡调用
     */
    @RequestMapping(value = "/sayHello3")
    public String sayHello3(String message){
        return consumerService.callProvider3(message);
    }

    /**
     * Feign调用
     */
    @RequestMapping(value = "/sayHello4")
    public String sayHello4(String message){
        return providerFacade.providerSayHello(message);
    }
}
