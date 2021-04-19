package com.ashen.xunwu.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/**
 * 基于Redis的Session配置
 * EnableRedisHttpSession注解自动开启Redis会话管理
 */
@Configuration
@EnableRedisHttpSession(maxInactiveIntervalInSeconds = 86400)  //一天过期
public class RedisSessionConfig {

    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory factory) {
        return new StringRedisTemplate(factory);
    }
}
