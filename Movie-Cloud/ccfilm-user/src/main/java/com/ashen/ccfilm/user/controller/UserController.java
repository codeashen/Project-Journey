package com.ashen.ccfilm.user.controller;

import com.ashen.ccfilm.common.exception.CommonServiceException;
import com.ashen.ccfilm.common.utils.JwtTokenUtil;
import com.ashen.ccfilm.common.vo.DataResult;
import com.ashen.ccfilm.user.entity.vo.LoginRequest;
import com.ashen.ccfilm.user.entity.vo.LoginResponse;
import com.ashen.ccfilm.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/user")
public class UserController {

    @Autowired
    private UserService userService;

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public DataResult<LoginResponse> login(@RequestBody LoginRequest request) throws CommonServiceException {
        // 数据验证
        request.checkParam();
        // 验证用户信息
        String userId = userService.checkUserLogin(request.getUsername(), request.getPassword());
        // 生成 randomKey 和 token
        String randomKey = JwtTokenUtil.getRandomKey();
        String token = JwtTokenUtil.generateToken(userId, randomKey);
        
        LoginResponse response = new LoginResponse();
        response.setRandomKey(randomKey);
        response.setToken(token);
        return DataResult.success(response);
    }
}
