package com.stylefeng.guns.api.cinema.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class FieldPriceVo implements Serializable {
    // 场次id
    private Integer fieldId;
    // 影院id
    private String cinemaId;
    // 价格
    private String filmPrice;
}
