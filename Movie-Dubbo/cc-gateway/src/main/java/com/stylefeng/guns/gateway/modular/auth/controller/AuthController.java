package com.stylefeng.guns.gateway.modular.auth.controller;

import com.stylefeng.guns.api.user.service.UserService;
import com.stylefeng.guns.gateway.common.vo.DataResult;
import com.stylefeng.guns.gateway.modular.auth.controller.dto.AuthRequest;
import com.stylefeng.guns.gateway.modular.auth.controller.dto.AuthResponse;
import com.stylefeng.guns.gateway.modular.auth.util.JwtTokenUtil;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 请求验证的
 */
@RestController
public class AuthController {

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @DubboReference(check = false)
    private UserService userService;

    @RequestMapping(value = "${jwt.auth-path}")
    public DataResult<AuthResponse> createAuthenticationToken(AuthRequest authRequest) {
        // 去掉guns自带的验证逻辑，使用自己的验证逻辑
        boolean validate = false;
        int userId = userService.login(authRequest.getUserName(), authRequest.getPassword());
        if (userId != 0) {
            validate = true;
        }

        if (validate) {
            // 生成randomKey和token
            final String randomKey = jwtTokenUtil.getRandomKey();
            final String token = jwtTokenUtil.generateToken(userId, randomKey);
            return DataResult.success(new AuthResponse(token, randomKey));
        } else {
            return DataResult.serviceFail("用户名或密码错误!");
        }
    }
}
