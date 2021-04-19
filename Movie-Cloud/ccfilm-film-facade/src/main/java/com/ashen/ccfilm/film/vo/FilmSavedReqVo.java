package com.ashen.ccfilm.film.vo;

import com.ashen.ccfilm.common.exception.CommonServiceException;
import com.ashen.ccfilm.common.vo.BaseRequestVo;
import lombok.Data;

/**
 * 保存影片信息请求
 */
@Data
public class FilmSavedReqVo extends BaseRequestVo {

    private String filmStatus;
    private String filmName;
    private String filmEnName;
    private String mainImgAddress;
    private String filmScore;
    private String filmScorers;
    private String preSaleNum;
    private String boxOffice;
    private String filmTypeId;
    private String filmSourceId;
    private String filmCatIds;
    private String areaId;
    private String dateId;
    private String filmTime;
    private String directorId;
    private String actIds;      // actIds与RoleNames是不是能在数量上对应上
    private String roleNames;
    private String filmLength;
    private String biography;
    private String filmImgs;

    @Override
    public void checkParam() throws CommonServiceException {
        
    }
}
