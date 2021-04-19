package com.ashen.ccfilm.common.vo;

import com.ashen.ccfilm.common.exception.CommonServiceException;

public abstract class BaseRequestVo {

    /**
     * 公共参数验证方法
     * @throws CommonServiceException 参数验证不通过时抛出异常
     */
    public void checkParam() throws CommonServiceException {
    }

}