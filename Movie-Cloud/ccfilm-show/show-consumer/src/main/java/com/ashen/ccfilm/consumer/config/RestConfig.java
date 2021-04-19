package com.ashen.ccfilm.consumer.config;

import com.netflix.loadbalancer.IPing;
import com.netflix.loadbalancer.IRule;
import com.netflix.loadbalancer.PingUrl;
import com.netflix.loadbalancer.RandomRule;
import com.netflix.niws.loadbalancer.NIWSDiscoveryPing;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestConfig {
    
    @Bean("restTemplate")
    public RestTemplate getRestTemplate() {
        return new RestTemplate();
    }

    @Bean("loadBalancedRestTemplate")
    @LoadBalanced  // 设置RestTemplate有负载均衡机制
    public RestTemplate getRestTemplate2() {
        return new RestTemplate();
    }

    /**
     * 负载均衡规则
     */
    @Bean
    public IRule iRule() {
        return new RandomRule();
    }
    
    @Bean
    public IPing iPing() {
        // isSecure：是否是安全链接， pingAppendString：Ping的uri
        return new PingUrl(false, "/provider/health");
        // return new NIWSDiscoveryPing();
    }
}
