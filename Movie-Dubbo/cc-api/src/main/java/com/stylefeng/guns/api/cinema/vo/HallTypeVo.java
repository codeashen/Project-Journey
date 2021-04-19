package com.stylefeng.guns.api.cinema.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class HallTypeVo implements Serializable {

    private String halltypeId;
    private String halltypeName;
    private boolean isActive;


}
