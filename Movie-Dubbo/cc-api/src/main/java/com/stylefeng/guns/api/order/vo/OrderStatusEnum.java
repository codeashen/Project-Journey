package com.stylefeng.guns.api.order.vo;

/**
 * 订单状态枚举
 */
public enum OrderStatusEnum {
    CREATED(0, "待支付"),
    PAID(1, "已支付"),
    CLOSED(2, "已关闭");
    
    private final Integer code;
    private final String msg;
    
    OrderStatusEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }
    
    public Integer code() {
        return this.code;
    }

    public String msg() {
        return this.msg;
    }

    public static boolean contains(Integer code) {
        if (code == null)
            return false;

        for (OrderStatusEnum value : OrderStatusEnum.values()) {
            if (value.code().equals(code)) {
                return true;
            }
        }
        return false;
    }

    public static OrderStatusEnum get(Integer code) {
        if (code == null)
            return null;

        for (OrderStatusEnum value : OrderStatusEnum.values()) {
            if (value.code().equals(code)) {
                return value;
            }
        }
        return null;
    }

    public boolean equals(Integer code) {
        if (code == null)
            return false;

        return code().equals(code);
    }
}
