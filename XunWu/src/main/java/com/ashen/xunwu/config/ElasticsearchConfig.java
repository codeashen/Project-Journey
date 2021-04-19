package com.ashen.xunwu.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ES配置类
 */
@Configuration
public class ElasticsearchConfig {

    /**
     * ES操作Java客户端
     */
    @Bean
    public RestHighLevelClient highLevelClient() {
        return new RestHighLevelClient(
                RestClient.builder(new HttpHost("localhost", 9200, "http"))
        );
    }
}
