package com.ashen.ccfilm.user.service;

import com.ashen.ccfilm.common.exception.CommonServiceException;

public interface UserService {
    String checkUserLogin(String username,String password) throws CommonServiceException;
}
