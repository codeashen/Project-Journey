package com.ashen.xunwu.web.dto;

import lombok.Data;

/**
 * 地铁站信息
 */
@Data
public class SubwayStationDTO {
    private Long id;
    private Long subwayId;
    private String name;
}
