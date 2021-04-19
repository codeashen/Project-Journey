package com.stylefeng.guns.user.modular.user.service;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.stylefeng.guns.api.user.service.UserService;
import com.stylefeng.guns.api.user.vo.UserInfoModel;
import com.stylefeng.guns.api.user.vo.UserModel;
import com.stylefeng.guns.core.util.BeanUtils;
import com.stylefeng.guns.core.util.MD5Util;
import com.stylefeng.guns.user.modular.user.dao.CcUserMapper;
import com.stylefeng.guns.user.modular.user.model.CcUser;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@DubboService(loadbalance = "roundrobin", filter = "myFilter")  //配置拦截器
public class UserServiceImpl implements UserService {

    @Autowired
    private CcUserMapper ccUserMapper;

    @Override
    public int login(String username, String password) {
        CcUser ccUser = new CcUser();
        ccUser.setUserName(username);

        CcUser result = ccUserMapper.selectOne(ccUser);
        if (result != null && result.getUuid() > 0) {
            String md5Password = MD5Util.encrypt(password);
            if (result.getUserPwd().equals(md5Password)) {
                return result.getUuid();
            }
        }
        return 0;
    }

    @Override
    public boolean register(UserModel userModel) {
        // 将注册信息实体转换为数据实体[mooc_user_t]
        CcUser ccUser = new CcUser();
        ccUser.setUserName(userModel.getUsername());
        ccUser.setEmail(userModel.getEmail());
        ccUser.setAddress(userModel.getAddress());
        ccUser.setUserPhone(userModel.getPhone());
        // 创建时间和修改时间 -> current_timestamp

        // 数据加密 【MD5混淆加密 + 盐值 -> Shiro加密】
        String md5Password = MD5Util.encrypt(userModel.getPassword());
        ccUser.setUserPwd(md5Password); // 注意

        // 将数据实体存入数据库
        Integer insert = ccUserMapper.insert(ccUser);
        return insert > 0;
    }

    @Override
    public boolean checkUsername(String username) {
        EntityWrapper<CcUser> entityWrapper = new EntityWrapper<>();
        entityWrapper.eq("user_name", username);
        Integer result = ccUserMapper.selectCount(entityWrapper);
        return result == null || result <= 0;
    }

    @Override
    public UserInfoModel getUserInfo(int uuid) {
        CcUser ccUser = ccUserMapper.selectById(uuid);
        return BeanUtils.copy(ccUser, UserInfoModel.class);
    }

    @Override
    public UserInfoModel updateUserInfo(UserInfoModel userInfoModel) {
        CcUser ccUser = BeanUtils.copy(userInfoModel, CcUser.class);
        ccUser.setBeginTime(null);
        ccUser.setUpdateTime(null);

        Integer updateCount = ccUserMapper.updateById(ccUser);
        if (updateCount > 0) {
            // 将数据从数据库中读取出来
            return getUserInfo(ccUser.getUuid());
        } else {
            return null;
        }
    }
}
