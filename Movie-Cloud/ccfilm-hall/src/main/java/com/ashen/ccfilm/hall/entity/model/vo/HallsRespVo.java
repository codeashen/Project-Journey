package com.ashen.ccfilm.hall.entity.model.vo;

import lombok.Data;

@Data
public class HallsRespVo {
    private String cinemaName;
    private String hallName;
    private String filmName;
    private String hallTypeName;
    private String beginTime;
    private String endTime;
    private String filmPrice;
}