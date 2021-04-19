package com.stylefeng.guns.gateway.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * jwt相关配置
 *
 * @author fengshuonan
 * @date 2017-08-23 9:23
 */
@Data
@Configuration
@ConfigurationProperties(prefix = JwtProperties.JWT_PREFIX)  // 将指定前缀的配置绑定到属性中
public class JwtProperties {

    public static final String JWT_PREFIX = "jwt";

    private String header = "Authorization";
    private String secret = "defaultSecret";
    private Long expiration = 604800L;
    private String authPath = "auth";
    private String md5Key = "randomKey";
    private String ignoreUrl = "";

    public static String getJwtPrefix() {
        return JWT_PREFIX;
    }
}
