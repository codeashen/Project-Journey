package com.ashen.ccfilm.consumer.service.impl;

import com.ashen.ccfilm.consumer.config.RestConfig;
import com.ashen.ccfilm.consumer.service.ConsumerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ConsumerServiceImpl implements ConsumerService {
    
    @Autowired
    private LoadBalancerClient loadBalancerClient;
    
    @Autowired
    @Qualifier("restTemplate")
    private RestTemplate restTemplate;
    @Autowired
    @Qualifier("loadBalancedRestTemplate")
    private RestTemplate loadBalancedRestTemplate; //加了LoadBalance注解的RestTemplate

    /**
     * 1. 写死方式，使用 RestTemplate
     * 不能负载均衡
     */
    @Override
    public String callProvider1(String message) {
        String url = "http://%s:%s/provider/sayHello?message=%s";
        String host = "localhost";
        int port = 7201;
        return restTemplate.getForObject(String.format(url, host, port, message), String.class);
    }

    /**
     * 2. 使用 LoadBalancerClient 动态获取 host 和 port
     * 可以负载均衡
     */
    @Override
    public String callProvider2(String message) {
        String url = "http://%s:%s/provider/sayHello?message=%s";
        ServiceInstance service = loadBalancerClient.choose("hello-service-provider");
        String host = service.getHost();
        int port = service.getPort();
        return restTemplate.getForObject(String.format(url, host, port, message), String.class);
    }

    /**
     * 3. 使用 RestTemplate 简化调用
     * 可以负载均衡，RestTemplate上加了 LoadBalance 注解
     * @see RestConfig#getRestTemplate()
     */
    @Override
    public String callProvider3(String message) {
        String uri = "/provider/sayHello?message=" + message;
        String url = "http://hello-service-provider" + uri;
        return loadBalancedRestTemplate.getForObject(url, String.class);
    }
}
