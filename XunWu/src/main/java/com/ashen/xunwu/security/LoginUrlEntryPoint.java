package com.ashen.xunwu.security;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * 基于角色的登录入口控制器
 */
public class LoginUrlEntryPoint extends LoginUrlAuthenticationEntryPoint {

    private static final String API_FREFIX = "/api";
    private static final String API_CODE_403 = "{\"code\": 403}";
    private static final String CONTENT_TYPE = "application/json;charset=UTF-8";
    
    // 地址匹配器
    private final PathMatcher pathMatcher = new AntPathMatcher();
    // 用户请求路径--登录入口 映射
    private final Map<String, String> authEntryPointMap;

    /**
     * @param loginFormUrl 登录页面URL
     */
    public LoginUrlEntryPoint(String loginFormUrl) {
        super(loginFormUrl);
        authEntryPointMap = new HashMap<>();
        authEntryPointMap.put("/user/**", "/user/login");   // 普通用户登录入口映射
        authEntryPointMap.put("/admin/**", "/admin/login"); // 管理员登录入口映射
    }

    /**
     * 根据请求跳转到指定的页面，父类是默认使用loginFormUrl
     * @param request
     * @param response
     * @param exception
     * @return
     */
    @Override
    protected String determineUrlToUseForThisRequest(HttpServletRequest request, HttpServletResponse response,
                                                     AuthenticationException exception) {
        // 获取请求uri
        String uri = request.getRequestURI().replace(request.getContextPath(), "");
        // 匹配路由地址
        for (Map.Entry<String, String> authEntry : this.authEntryPointMap.entrySet()) {
            if (this.pathMatcher.match(authEntry.getKey(), uri)) {
                return authEntry.getValue();
            }
        }
        // 没匹配到走默认逻辑
        return super.determineUrlToUseForThisRequest(request, response, exception);
    }

    /**
     * 如果是Api接口 返回json数据 否则按照一般流程处理
     * @param request
     * @param response
     * @param authException
     * @throws IOException
     * @throws ServletException
     */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        String uri = request.getRequestURI();
        if (uri.startsWith(API_FREFIX)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType(CONTENT_TYPE);

            PrintWriter pw = response.getWriter();
            pw.write(API_CODE_403);
            pw.close();
        } else {
            super.commence(request, response, authException);
        }
    }
    
}
