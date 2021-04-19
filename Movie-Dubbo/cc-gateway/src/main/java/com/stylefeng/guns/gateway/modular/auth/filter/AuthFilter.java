package com.stylefeng.guns.gateway.modular.auth.filter;

import com.stylefeng.guns.core.base.tips.ErrorTip;
import com.stylefeng.guns.core.util.RenderUtil;
import com.stylefeng.guns.gateway.config.properties.JwtProperties;
import com.stylefeng.guns.gateway.modular.auth.util.JwtTokenUtil;
import com.stylefeng.guns.gateway.common.CurrentUserHolder;
import com.stylefeng.guns.gateway.common.exception.BizExceptionEnum;
import io.jsonwebtoken.JwtException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 对客户端请求的jwt token验证过滤器
 *
 * @author fengshuonan
 * @Date 2017/8/24 14:04
 */
public class AuthFilter extends OncePerRequestFilter {

    private final Log logger = LogFactory.getLog(this.getClass());

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private JwtProperties jwtProperties;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        String servletPath = request.getServletPath();
        if (servletPath.equals("/" + jwtProperties.getAuthPath())) {
            chain.doFilter(request, response);
            return;
        }
        // 配置忽略列表
        String ignoreUrlStr = jwtProperties.getIgnoreUrl();
        if (StringUtils.isNotBlank(ignoreUrlStr)) {
            String[] ignoreUrls = ignoreUrlStr.split(",");
            for (String ignoreUrl : ignoreUrls) {
                // 通配符，或者完全匹配都不拦截
                if ((ignoreUrl.endsWith("*") && servletPath.startsWith(ignoreUrl.substring(0, ignoreUrl.length() - 1)))
                        || (servletPath.equals(ignoreUrl))) {
                    chain.doFilter(request, response);
                    return;
                }
            }
        }

        // 从请求header中获取token,进行校验
        final String requestHeader = request.getHeader(jwtProperties.getHeader());
        String authToken = null;
        if (requestHeader != null && requestHeader.startsWith("Bearer ")) {
            authToken = requestHeader.substring(7);
            try {
                //验证token是否过期,包含了验证jwt是否正确
                if (jwtTokenUtil.isTokenExpired(authToken)) {
                    RenderUtil.renderJson(response, new ErrorTip(BizExceptionEnum.TOKEN_EXPIRED.getCode(), BizExceptionEnum.TOKEN_EXPIRED.getMessage()));
                    return;
                }
                // 获取token中的userId，存入ThreadLocal
                Integer userId = jwtTokenUtil.getUserIdFromToken(authToken);
                if (userId == null || userId == 0) {
                    RenderUtil.renderJson(response, new ErrorTip(BizExceptionEnum.SIGN_ERROR.getCode(), BizExceptionEnum.SIGN_ERROR.getMessage()));
                    return;
                }
                CurrentUserHolder.saveUserId(userId);
            } catch (JwtException e) {
                //有异常就是token解析失败
                RenderUtil.renderJson(response, new ErrorTip(BizExceptionEnum.TOKEN_ERROR.getCode(), BizExceptionEnum.TOKEN_ERROR.getMessage()));
                return;
            }
        } else {
            //header没有带Bearer字段
            RenderUtil.renderJson(response, new ErrorTip(BizExceptionEnum.TOKEN_ERROR.getCode(), BizExceptionEnum.TOKEN_ERROR.getMessage()));
            return;
        }
        chain.doFilter(request, response);
    }
}