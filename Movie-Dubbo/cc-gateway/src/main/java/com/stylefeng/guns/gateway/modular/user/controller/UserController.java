package com.stylefeng.guns.gateway.modular.user.controller;

import com.stylefeng.guns.api.user.service.UserService;
import com.stylefeng.guns.api.user.vo.UserInfoModel;
import com.stylefeng.guns.api.user.vo.UserModel;
import com.stylefeng.guns.gateway.common.CurrentUserHolder;
import com.stylefeng.guns.gateway.common.vo.DataResult;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {

    @DubboReference(check = false)
    private UserService userService;

    @RequestMapping(value = "register", method = RequestMethod.POST)
    public DataResult<String> register(UserModel userModel) {
        if (StringUtils.isBlank(userModel.getUsername())) {
            return DataResult.serviceFail("用户名不能为空");
        }
        if (StringUtils.isBlank(userModel.getPassword())) {
            return DataResult.serviceFail("密码不能为空");
        }

        boolean isSuccess = userService.register(userModel);
        if (isSuccess) {
            return DataResult.success("注册成功");
        } else {
            return DataResult.serviceFail("注册失败");
        }
    }

    @RequestMapping(value = "check", method = RequestMethod.POST)
    public DataResult<String> check(String username) {
        if (StringUtils.isNotBlank(username)) {
            // 当返回true的时候，表示用户名可用
            boolean notExists = userService.checkUsername(username);
            if (notExists) {
                return DataResult.success("用户名不存在");
            } else {
                return DataResult.serviceFail("用户名已存在");
            }
        } else {
            return DataResult.serviceFail("用户名不能为空");
        }
    }

    @RequestMapping(value = "logout", method = RequestMethod.GET)
    public DataResult<String> logout() {
        /*
            应用：
                1、前端存储JWT 【七天】 ： JWT的刷新
                2、服务器端会存储活动用户信息【30分钟】
                3、JWT里的userId为key，查找活跃用户
            退出：
                1、前端删除掉JWT
                2、后端服务器删除活跃用户缓存
            现状：
                1、前端删除掉JWT
         */
        return DataResult.success("用户退出成功");
    }


    @RequestMapping(value = "getUserInfo", method = RequestMethod.GET)
    public DataResult<UserInfoModel> getUserInfo() {
        // 获取当前登陆用户
        Integer userId = CurrentUserHolder.getCurrentUser();
        if (userId != null && userId != 0) {
            // 将用户ID传入后端进行查询
            UserInfoModel userInfo = userService.getUserInfo(userId);
            if (userInfo != null) {
                return DataResult.success(userInfo);
            } else {
                return DataResult.appFail("用户信息查询失败");
            }
        } else {
            return DataResult.serviceFail("用户未登陆");
        }
    }

    @RequestMapping(value = "updateUserInfo", method = RequestMethod.POST)
    public DataResult<UserInfoModel> updateUserInfo(UserInfoModel userInfoModel) {
        // 获取当前登陆用户
        Integer userId = CurrentUserHolder.getCurrentUser();
        if (userId != null && userId != 0) {
            // 判断当前登陆人员的ID与修改的结果ID是否一致
            if (!userId.equals(userInfoModel.getUuid())) {
                return DataResult.serviceFail("禁止操作他人信息");
            }

            UserInfoModel userInfo = userService.updateUserInfo(userInfoModel);
            if (userInfo != null) {
                return DataResult.success(userInfo);
            } else {
                return DataResult.appFail("用户信息修改失败");
            }
        } else {
            return DataResult.serviceFail("用户未登陆");
        }
    }
}
