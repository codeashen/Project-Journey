package com.ashen.xunwu.base;

import lombok.Data;

/**
 * Datatables响应结构
 */
@Data
public class ApiDataTableResponse extends ApiResponse<Object> {
    
    // 前端Datatables要求的字段
    private int draw;
    private long recordsTotal;
    private long recordsFiltered;

    public ApiDataTableResponse(ApiResponse.Status status) {
        this(status.getCode(), status.getStandardMessage(), null);
    }

    public ApiDataTableResponse(int code, String message, Object data) {
        super(code, message, data);
    }
}
