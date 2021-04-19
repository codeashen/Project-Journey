package com.ashen.xunwu.service.search.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 百度位置信息
 */
@Data
public class BaiduMapLocation {
    // 经度
    @JsonProperty("lon")  //es中geo_point要求名称
    private double longitude;
    
    // 纬度
    @JsonProperty("lat")
    private double latitude;
}
