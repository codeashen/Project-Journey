package com.ashen.xunwu.security;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 登录验证失败处理器
 */
public class LoginAuthFailHandler extends SimpleUrlAuthenticationFailureHandler {
    
    private final LoginUrlEntryPoint urlEntryPoint;

    public LoginAuthFailHandler(LoginUrlEntryPoint urlEntryPoint) {
        this.urlEntryPoint = urlEntryPoint;
    }

    /**
     * 执行重定向或转发到指定URL
     */
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        // 通过登录入口控制器，获取登录入口URL，加上错误信息，组成目标URL
        String targetUrl = this.urlEntryPoint.determineUrlToUseForThisRequest(request, response, exception);
        targetUrl += "?" + exception.getMessage();
        
        super.setDefaultFailureUrl(targetUrl);  //设置用作失败目标的URL
        super.onAuthenticationFailure(request, response, exception);
    }
}
