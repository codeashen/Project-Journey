package com.ashen.xunwu.base;

/**
 * 房源状态
 */
public enum HouseStatusEnum {
    NOT_AUDITED(0), // 未审核
    PASSES(1), // 审核通过
    RENTED(2), // 已出租
    DELETED(3); // 逻辑删除
    private int value;

    HouseStatusEnum(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
