package com.ashen.ccfilm.common.exception;

import com.ashen.ccfilm.common.vo.DataResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

/**
 * 公共异常处理器
 */
@Slf4j
@ControllerAdvice
public class BaseExceptionHandler {

    /**
     * 通用异常处理
     * @param request 请求对象
     * @param e       发生的异常
     * @return 返回的通用响应
     */
    @ExceptionHandler(CommonServiceException.class)
    @ResponseBody
    public DataResult<String> serviceExceptionHandler(HttpServletRequest request, CommonServiceException e) {
        log.error("请求出错:{}, 错误码:{}, 错误信息:{}", request.getServletPath(), e.getCode(), e.getMessage());
        return DataResult.serviceException(e);
    }

}