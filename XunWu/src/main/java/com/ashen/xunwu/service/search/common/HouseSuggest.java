package com.ashen.xunwu.service.search.common;

import lombok.Data;

/**
 * 房屋关键词
 */
@Data
public class HouseSuggest {
    private String input;
    private int weight = 10; // 默认权重
}
