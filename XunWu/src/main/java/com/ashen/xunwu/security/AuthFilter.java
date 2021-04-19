package com.ashen.xunwu.security;

import com.ashen.xunwu.base.LoginUserUtil;
import com.ashen.xunwu.entity.User;
import com.ashen.xunwu.service.user.ISmsService;
import com.ashen.xunwu.service.user.IUserService;
import org.elasticsearch.common.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Objects;

/**
 * 处理身份验证表单提交
 */
public class AuthFilter extends UsernamePasswordAuthenticationFilter {

    @Autowired
    private IUserService userService;
    @Autowired
    private ISmsService smsService;

    /**
     * 登录认证方法
     */
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {
        // 获取用户名，用户名不为空则走传统登录验证流程
        String name = obtainUsername(request);
        if (!Strings.isNullOrEmpty(name)) {
            request.setAttribute("username", name);
            return super.attemptAuthentication(request, response);
        }

        // 没有用户名，获取手机号，走手机号登录
        String telephone = request.getParameter("telephone");
        if (Strings.isNullOrEmpty(telephone) || !LoginUserUtil.checkTelephone(telephone)) {
            throw new BadCredentialsException("Wrong telephone number");
        }

        // 查询用户信息、输入验证码、缓存的验证码
        User user = userService.findUserByPhone(telephone);
        String inputCode = request.getParameter("smsCode");
        String sessionCode = smsService.getSmsCode(telephone);
        
        // 验证码校验
        if (Objects.equals(inputCode, sessionCode)) {
            if (user == null) { // 如果用户第一次用手机登录 则自动注册该用户
                user = userService.addUserByPhone(telephone);
            }
            return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        } else {
            throw new BadCredentialsException("smsCodeError");
        }
    }
    
}
