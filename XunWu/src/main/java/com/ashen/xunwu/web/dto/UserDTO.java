package com.ashen.xunwu.web.dto;

import lombok.Data;

/**
 * 用户信息
 */
@Data
public class UserDTO {
    private Long id;
    private String name;
    private String avatar;
    private String phoneNumber;
    private String lastLoginTime;
}
