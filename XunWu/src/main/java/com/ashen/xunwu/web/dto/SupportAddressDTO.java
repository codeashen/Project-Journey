package com.ashen.xunwu.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 支持的地址
 */
@Data
public class SupportAddressDTO {
    private Long id;
    
    @JsonProperty(value = "belong_to")
    private String belongTo;

    @JsonProperty(value = "en_name")
    private String enName;

    @JsonProperty(value = "cn_name")
    private String cnName;

    private String level;

    // 百度地图经度
    private double baiduMapLongitude;
    // 百度地图纬度
    private double baiduMapLatitude;
}
