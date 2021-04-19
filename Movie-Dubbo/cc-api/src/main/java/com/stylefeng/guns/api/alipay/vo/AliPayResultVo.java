package com.stylefeng.guns.api.alipay.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class AliPayResultVo implements Serializable {

    private String orderId;
    private Integer orderStatus;
    private String orderMsg;

}
