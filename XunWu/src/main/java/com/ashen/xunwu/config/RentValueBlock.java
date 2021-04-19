package com.ashen.xunwu.config;

import java.util.Map;

import com.google.common.collect.ImmutableMap;
import lombok.Data;

/**
 * 带区间的常用数值定义
 */
@Data
public class RentValueBlock {
    
    /**
     * 价格区间定义
     */
    public static final Map<String, RentValueBlock> PRICE_BLOCK;

    /**
     * 面积区间定义
     */
    public static final Map<String, RentValueBlock> AREA_BLOCK;

    /**
     * 无限制区间
     */
    public static final RentValueBlock ALL = new RentValueBlock("*", -1, -1);

    static {
        // 初始化价格区间
        PRICE_BLOCK = ImmutableMap.<String, RentValueBlock>builder()
                .put("*-1000", new RentValueBlock("*-1000", -1, 1000))
                .put("1000-3000", new RentValueBlock("1000-3000", 1000, 3000))
                .put("3000-*", new RentValueBlock("3000-*", 3000, -1))
                .build();
        // 初始化面积区间
        AREA_BLOCK = ImmutableMap.<String, RentValueBlock>builder()
                .put("*-30", new RentValueBlock("*-30", -1, 30))
                .put("30-50", new RentValueBlock("30-50", 30, 50))
                .put("50-*", new RentValueBlock("50-*", 50, -1))
                .build();
    }

    private String key; //区间标识符
    private int min;    //区间最小值
    private int max;    //区间最大值

    public RentValueBlock(String key, int min, int max) {
        this.key = key;
        this.min = min;
        this.max = max;
    }

    /**
     * 根据指定标识符获得对应的价格区间
     */
    public static RentValueBlock matchPrice(String key) {
        RentValueBlock block = PRICE_BLOCK.get(key);
        if (block == null) {
            return ALL;
        }
        return block;
    }

    /**
     * 根据指定标识符获得对应的面积区间
     */
    public static RentValueBlock matchArea(String key) {
        RentValueBlock block = AREA_BLOCK.get(key);
        if (block == null) {
            return ALL;
        }
        return block;
    }
}
