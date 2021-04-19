package com.ashen.ccfilm.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ashen.ccfilm.common.exception.CommonServiceException;
import com.ashen.ccfilm.common.utils.MD5Util;
import com.ashen.ccfilm.user.dao.UserMapper;
import com.ashen.ccfilm.user.entity.model.User;
import com.ashen.ccfilm.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public String checkUserLogin(String username, String password) throws CommonServiceException {
        // 根据用户名获取用户信息
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_name", username);
 
        // 避免数据出现问题
        List<User> list = userMapper.selectList(queryWrapper);
        User user = null;
        if (!CollectionUtils.isEmpty(list)) {
            user = list.get(0);
        } else {
            throw new CommonServiceException(404, "用户名输入有误");
        }

        // 验证密码是否正确【密码要做MD5加密，才能验证是否匹配】
        String encrypt = MD5Util.encrypt(password);

        if (!encrypt.equals(user.getUserPwd())) {
            throw new CommonServiceException(500, "用户密码输入有误");
        } else {
            return user.getUuid().toString();
        }
    }
}
