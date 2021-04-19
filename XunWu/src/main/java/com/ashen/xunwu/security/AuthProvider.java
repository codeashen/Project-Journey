package com.ashen.xunwu.security;

import com.ashen.xunwu.entity.User;
import com.ashen.xunwu.service.user.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 自定义认证实现
 */
public class AuthProvider implements AuthenticationProvider {
    
    @Autowired
    private IUserService userService;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * 身份认证
     * @param authentication 身份验证请求对象
     * @return 包含凭据的完全经过身份验证的对象
     * @throws AuthenticationException 如果身份验证失败
     */
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        // 获取用户名和认证凭证
        String userName = authentication.getName(); 
        String inputPassword = (String) authentication.getCredentials(); 
        
        // 查询用户信息
        User user = userService.findUserByName(userName);
        if (user == null) {
            throw new AuthenticationCredentialsNotFoundException("authError");
        }
        
        // 认证信息对比
        if (passwordEncoder.matches(inputPassword, user.getPassword())) {
            // 返回包含凭据的完全经过身份验证的对象
            return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        }
        throw new BadCredentialsException("authError");
    }

    /**
     * 返回是否支持Authentication
     */
    @Override
    public boolean supports(Class<?> aClass) {
        return true;
    }
}
