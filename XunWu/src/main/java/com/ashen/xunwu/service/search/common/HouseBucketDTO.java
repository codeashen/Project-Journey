package com.ashen.xunwu.service.search.common;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * ES分组查询bucket对象
 */
@Data
@AllArgsConstructor
public class HouseBucketDTO {
    /**
     * 聚合bucket的key
     */
    private String key;

    /**
     * 聚合结果值
     */
    private long count;
}
