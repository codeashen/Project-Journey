package com.stylefeng.guns.gateway;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * Guns Gateway Web程序启动类
 *
 * @author fengshuonan
 * @date 2017年9月29日09:00:42
 */
public class GunsGatewayServletInitializer extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(GunsGatewayApplication.class);
    }

}
