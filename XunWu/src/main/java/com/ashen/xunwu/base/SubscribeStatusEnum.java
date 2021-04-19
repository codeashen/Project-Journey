package com.ashen.xunwu.base;

/**
 * 预约状态码
 */
public enum SubscribeStatusEnum {
    NO_SUBSCRIBE(0), // 未预约
    IN_ORDER_LIST(1), // 已加入待看清单
    IN_ORDER_TIME(2), // 已经预约看房时间
    FINISH(3); // 已完成预约

    private int value;

    SubscribeStatusEnum(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static SubscribeStatusEnum of(int value) {
        for (SubscribeStatusEnum status : SubscribeStatusEnum.values()) {
            if (status.getValue() == value) {
                return status;
            }
        }
        return SubscribeStatusEnum.NO_SUBSCRIBE;
    }
}
