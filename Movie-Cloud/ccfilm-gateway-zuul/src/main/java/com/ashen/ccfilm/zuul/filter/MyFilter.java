package com.ashen.ccfilm.zuul.filter;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Enumeration;

/**
 * Zuul网关自定义Filter
 */
@Slf4j
public class MyFilter extends ZuulFilter {
    // filter类型
    @Override
    public String filterType() {
        return "pre";
    }

    // filter执行顺序，越大越靠后
    @Override
    public int filterOrder() {
        return 0;
    }

    // 是否启用
    @Override
    public boolean shouldFilter() {
        return true;
    }

    // filter具体业务逻辑
    @Override
    public Object run() throws ZuulException {
        // Zuul的请求上下文
        RequestContext requestContext = RequestContext.getCurrentContext();
        // 可以通过请求上下文获取request、response等
        HttpServletRequest request = requestContext.getRequest();
        HttpServletResponse response = requestContext.getResponse();

        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()){
            String headName = headerNames.nextElement();
            log.warn("headName:{}, headValue:{}", headName, request.getHeader(headName));
        }
        return null;
    }
}
