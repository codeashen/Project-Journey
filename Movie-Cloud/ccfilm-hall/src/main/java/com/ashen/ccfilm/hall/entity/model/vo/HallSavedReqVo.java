package com.ashen.ccfilm.hall.entity.model.vo;

import com.ashen.ccfilm.common.exception.CommonServiceException;
import com.ashen.ccfilm.common.vo.BaseRequestVo;
import lombok.Data;

@Data
public class HallSavedReqVo extends BaseRequestVo {

    private String cinemaId;
    private String filmId;
    private String hallTypeId;
    private String beginTime;
    private String endTime;
    private String filmPrice;
    private String hallName;

    @Override
    public void checkParam() throws CommonServiceException {

    }
}
