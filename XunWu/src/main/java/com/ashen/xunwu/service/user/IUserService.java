package com.ashen.xunwu.service.user;

import com.ashen.xunwu.entity.User;
import com.ashen.xunwu.service.ServiceResult;
import com.ashen.xunwu.web.dto.UserDTO;

public interface IUserService {
    User findUserByName(String username);

    ServiceResult<UserDTO> findById(Long userId);

    /**
     * 根据电话号码寻找用户
     * @param telephone
     * @return
     */
    User findUserByPhone(String telephone);

    /**
     * 通过手机号添加用户
     * @param telephone
     * @return
     */
    User addUserByPhone(String telephone);

    /**
     * 修改指定用户信息
     * @param profile
     * @param value
     * @return
     */
    ServiceResult<String> modifyUserProfile(String profile, String value);
}
