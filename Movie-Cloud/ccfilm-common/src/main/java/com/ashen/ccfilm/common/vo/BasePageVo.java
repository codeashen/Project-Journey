package com.ashen.ccfilm.common.vo;

import com.ashen.ccfilm.common.exception.CommonServiceException;
import lombok.Data;

@Data
public class BasePageVo extends BaseRequestVo {
    private Integer nowPage = 1;
    private Integer pageSize = 10;

    @Override
    public void checkParam() throws CommonServiceException {
        if (nowPage < 1 || pageSize < 0) {
            throw new CommonServiceException(400, "分页参数错误");
        }
    }
}
