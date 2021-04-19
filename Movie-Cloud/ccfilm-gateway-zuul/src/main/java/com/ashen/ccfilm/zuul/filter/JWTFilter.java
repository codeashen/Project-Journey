package com.ashen.ccfilm.zuul.filter;

import com.alibaba.fastjson.JSONObject;
import com.ashen.ccfilm.common.properties.JwtProperties;
import com.ashen.ccfilm.common.utils.JwtTokenUtil;
import com.ashen.ccfilm.common.vo.DataResult;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * JWT验证拦截器
 */
@Slf4j
public class JWTFilter extends ZuulFilter {
    // Filter类型
    @Override
    public String filterType() {
        return "pre";
    }

    // filter的执行顺序
    @Override
    public int filterOrder() {
        return 0;
    }

    // 是否要拦截
    @Override
    public boolean shouldFilter() {
        return true;
    }

    // Filter的具体业务逻辑
    @Override
    public Object run() throws ZuulException {
        // 获取相关加密属性
        JwtProperties jwtProperties = JwtProperties.getJwtProperties();

        // ThreadLocal
        RequestContext ctx = RequestContext.getCurrentContext();
        // 获取当前请求和返回值
        HttpServletRequest request = ctx.getRequest();
        HttpServletResponse response = ctx.getResponse();

        // 提前设置请求继续，如果失败则修改此内容
        ctx.setSendZuulResponse(true);
        ctx.setResponseStatusCode(200);

        // 判断是否是登陆，如果是登陆则不验证JWT
        if (request.getServletPath().endsWith("/" + jwtProperties.getAuthPath())) {
            return null;
        }

        // 1、验证Token有效性 -> 用户是否登录过
        final String authToken = request.getHeader(jwtProperties.getHeader());
        // Bearer header.payload.sign
        if (authToken != null) {
            //验证token是否过期,包含了验证jwt是否正确
            try {
                boolean flag = JwtTokenUtil.isTokenExpired(authToken);
                if (flag) {
                    renderJson(ctx, response, DataResult.noLogin());
                } else {
                    // 2、解析出JWT中的payload -> userid - randomkey
                    String randomkey = JwtTokenUtil.getMd5KeyFromToken(authToken);
                    String userId = JwtTokenUtil.getUsernameFromToken(authToken);
                    // 3、是否需要验签,以及验签的算法

                    // 4、判断userid是否有效
                    // TODO
                }
            } catch (JwtException e) {
                //有异常就是token解析失败
                renderJson(ctx, response, DataResult.noLogin());
            }
        } else {
            //header没有带Bearer字段
            renderJson(ctx, response, DataResult.noLogin());
        }

        return null;
    }

    /**
     * 渲染json对象
     */
    public static void renderJson(RequestContext ctx, HttpServletResponse response, Object jsonObject) {
        // 设置终止请求
        response.setHeader("Content-Type", "application/json;charset=UTF-8");
        ctx.setSendZuulResponse(false);
        ctx.setResponseBody(JSONObject.toJSONString(jsonObject));
    }

}
