package com.ashen.ccfilm.provider.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/provider")
public class ProviderController {

    @Value("${server.port}")
    private int port;

    @RequestMapping(value = "/sayHello", method = RequestMethod.GET)
    public String providerSayHello(String message) {
        String response = String.format("provider sayHello port:%s, message:%s", port, message);
        log.error(response);
        return response;
    }

    @RequestMapping(value = "/health", method = RequestMethod.GET)
    public void providerSayHello() {
        log.info("=== 健康检查接口被调用 ===");
    }
}
