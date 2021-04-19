package com.ashen.ccfilm.film.vo;

import lombok.Data;

/**
 * 查询影片列表接口返回对象
 */
@Data
public class DescribeFilmsRespVo {
    private String filmId;
    private String filmStatus;
    private String filmName;
    private String filmEnName;
    private String filmScore;
    private String preSaleNum;
    private String boxOffice;
    private String filmTime;
    private String filmLength;
    private String mainImg;
}
