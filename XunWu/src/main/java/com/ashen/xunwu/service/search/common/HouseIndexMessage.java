package com.ashen.xunwu.service.search.common;

import lombok.Data;

/**
 * Kafka消息实体
 */
@Data
public class HouseIndexMessage {

    public static final String INDEX = "index";     // 更新操作
    public static final String REMOVE = "remove";   // 删除操作
    public static final int MAX_RETRY = 3;          // 最大重试次数
    
    private Long houseId;       // 房源id
    private String operation;   // 操作类型
    private int retry = 0;      // 重试次数

    /**
     * 默认构造器 防止jackson序列化失败
     */
    public HouseIndexMessage() {
    }

    public HouseIndexMessage(Long houseId, String operation, int retry) {
        this.houseId = houseId;
        this.operation = operation;
        this.retry = retry;
    }
}

