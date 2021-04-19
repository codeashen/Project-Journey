package com.ashen.ccfilm.user.entity.vo;

import lombok.Data;

@Data
public class LoginResponse {
    private String randomKey;
    private String token;
}
