package com.ashen.ccfilm.common.vo;

import com.ashen.ccfilm.common.exception.CommonServiceException;
import lombok.Data;

@Data
public class DataResult<M> {

    private Integer code;   // 业务编号
    private String message; // 异常信息
    private M data;         // 业务数据返回

    private DataResult() {
    }

    // 成功但是无参数
    public static DataResult<String> success() {
        DataResult<String> response = new DataResult<>();
        response.setCode(200);
        response.setMessage("");
        return response;
    }

    // 成功有参数
    public static <M> DataResult<M> success(M data) {
        DataResult<M> response = new DataResult<>();
        response.setCode(200);
        response.setMessage("");
        response.setData(data);
        return response;
    }

    // 未登录异常
    public static <M> DataResult<M> noLogin() {
        DataResult<M> response = new DataResult<>();
        response.setCode(401);
        response.setMessage("请登录");
        return response;
    }

    // 出现业务异常
    public static <M> DataResult<M> serviceException(CommonServiceException e) {
        DataResult<M> response = new DataResult<>();
        response.setCode(e.getCode());
        response.setMessage(e.getMessage());
        return response;
    }
}
