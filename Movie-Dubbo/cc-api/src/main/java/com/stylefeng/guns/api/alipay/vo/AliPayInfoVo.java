package com.stylefeng.guns.api.alipay.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class AliPayInfoVo implements Serializable {

    private String orderId;
    private String QRCodeAddress;

}
