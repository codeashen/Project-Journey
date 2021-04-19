package com.ashen.ccfilm.zuul.filter;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;

/**
 * 解决跨域问题Filter
 */
@Component
public class CorsFilter extends ZuulFilter {

    public String filterType() {
        return "pre";
    }

    public int filterOrder() {
        return 0;
    }

    public boolean shouldFilter() {
        return true;
    }

    public Object run() {
        RequestContext ctx = RequestContext.getCurrentContext();
        // 跨域
        HttpServletResponse response = ctx.getResponse();
        response.addHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET,POST,OPTIONS,DELETE,PUT");
        response.setHeader("Access-Control-Allow-Headers", "DNT,User-Agent,X-Requested-With,If-Modified-Since,Cache-Control,Content-Type,Range,Authorization");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        // response.setContentType("text/html;charset=UTF-8");

        /*
            跨域资源共享
                - 这是HTTP协议规定的安全策略
                - 配置资源共享的方式和目标方
            
            前端： node+vue -> admin.ccfilm.com
            后端： springboot -> backend.ccfilm.com
            
            -> 示例
            缺陷：如果出现跨域策略不足的情况，需要修改代码，重新部署
            优化：跨域问题配置到 Nginx
        */

        return null;
    }

}