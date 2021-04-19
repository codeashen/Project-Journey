package com.ashen.ccfilm.common.properties;

import lombok.Data;

@Data
public class JwtProperties {

    private static JwtProperties jwtProperties = new JwtProperties();

    private JwtProperties() {
    }

    public static JwtProperties getJwtProperties() {
        return jwtProperties;
    }

    public static final String JWT_PREFIX = "jwt";

    // 认证的请求头
    private String header = "Authorization";
    // 加密的形式
    private String secret = "defaultSecret";
    // token有效时间
    private Long expiration = 604800L;  //七天
    // 办法token的请求路径
    private String authPath = "login";
    // md5加密的key
    private String md5Key = "randomKey";

    public static String getJwtPrefix() {
        return JWT_PREFIX;
    }
}
