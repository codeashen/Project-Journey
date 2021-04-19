package com.ashen.ccfilm.cinema.entity.vo;

import com.ashen.ccfilm.common.exception.CommonServiceException;
import com.ashen.ccfilm.common.vo.BaseRequestVo;
import lombok.Data;

@Data
public class CinemaSavedReqVo extends BaseRequestVo {

    private String brandId;
    private String areaId;
    private String hallTypeIds;
    private String cinemaName;
    private String cinemaAddress;
    private String cinemaTele;
    private String cinemaImgAddress;
    private String cinemaPrice;

    @Override
    public void checkParam() throws CommonServiceException {

    }
}

