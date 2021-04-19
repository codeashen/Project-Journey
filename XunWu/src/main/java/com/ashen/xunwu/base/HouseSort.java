package com.ashen.xunwu.base;

import com.google.common.collect.Sets;
import org.springframework.data.domain.Sort;

import java.util.Set;

/**
 * 排序生成器
 */
public class HouseSort {
    // 默认排序字段，更新时间
    public static final String DEFAULT_SORT_KEY = "lastUpdateTime";
    // 与地铁距离
    public static final String DISTANCE_TO_SUBWAY_KEY = "distanceToSubway";
    // 排序字段集
    private static final Set<String> SORT_KEYS = Sets.newHashSet(
            DEFAULT_SORT_KEY,
            DISTANCE_TO_SUBWAY_KEY,
            "createTime",
            "price",
            "area"
    );

    // public static Sort generateSort(String key, String directionKey) {
    //     key = getSortKey(key);
    //
    //     Sort.Direction direction = Sort.Direction.fromStringOrNull(directionKey);
    //     if (direction == null) {
    //         direction = Sort.Direction.DESC;
    //     }
    //
    //     return new Sort(direction, key);
    // }

    public static String getSortKey(String key) {
        if (!SORT_KEYS.contains(key)) {
            key = DEFAULT_SORT_KEY;
        }

        return key;
    }
}
