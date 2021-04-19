package com.stylefeng.guns.gateway.modular.auth.controller.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 认证的请求dto
 *
 * @author fengshuonan
 * @Date 2017/8/24 14:00
 */
@Data
public class AuthRequest implements Serializable {

    private String userName;
    private String password;
    
}
