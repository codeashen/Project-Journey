package com.ashen.ccfilm.hall.entity.model.vo;

import com.ashen.ccfilm.common.exception.CommonServiceException;
import com.ashen.ccfilm.common.vo.BasePageVo;
import lombok.Data;

@Data
public class HallsReqVo extends BasePageVo {

    private String cinemaId;

    @Override
    public void checkParam() throws CommonServiceException {
        super.checkParam();
    }
}
