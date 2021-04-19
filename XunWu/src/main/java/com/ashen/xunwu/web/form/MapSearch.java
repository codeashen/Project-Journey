package com.ashen.xunwu.web.form;

import lombok.Data;

/**
 * 百度地图搜索实体
 */
@Data
public class MapSearch {
    private String cityEnName;

    /**
     * 地图缩放级别
     */
    private int level = 12;
    private String orderBy = "lastUpdateTime";
    private String orderDirection = "desc";
    /**
     * 左上角经纬度
     */
    private Double leftLongitude;
    private Double leftLatitude;

    /**
     * 右下角经纬度
     */
    private Double rightLongitude;
    private Double rightLatitude;

    private int start = 0;
    private int size = 5;
    
}
