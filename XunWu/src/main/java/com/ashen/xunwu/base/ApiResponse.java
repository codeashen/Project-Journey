package com.ashen.xunwu.base;

import lombok.Data;
import lombok.Getter;

/**
 * API格式封装
 */
@Data
public class ApiResponse<T> {
    private int code;
    private String message;
    private T data;
    private boolean more;

    public ApiResponse() {
        this.code = Status.SUCCESS.getCode();
        this.message = Status.SUCCESS.getStandardMessage();
    }

    public ApiResponse(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <V> ApiResponse<V> ofMessage(int code, String message) {
        return new ApiResponse<>(code, message, null);
    }

    public static <V> ApiResponse<V> ofSuccess() {
        return new ApiResponse<>();
    }
    
    public static <V> ApiResponse<V> ofSuccess(V data) {
        return new ApiResponse<>(Status.SUCCESS.getCode(), Status.SUCCESS.getStandardMessage(), data);
    }

    public static <V> ApiResponse<V> ofStatus(Status status) {
        return new ApiResponse<>(status.getCode(), status.getStandardMessage(), null);
    }

    public enum Status {
        SUCCESS(200, "OK"),
        BAD_REQUEST(400, "Bad Request"),
        NOT_FOUND(404, "Not Found"),
        INTERNAL_SERVER_ERROR(500, "Unknown Internal Error"),
        NOT_VALID_PARAM(40005, "Not valid Params"),
        NOT_SUPPORTED_OPERATION(40006, "Operation not supported"),
        NOT_LOGIN(50000, "Not Login");

        @Getter
        private final int code;
        @Getter
        private final String standardMessage;

        Status(int code, String standardMessage) {
            this.code = code;
            this.standardMessage = standardMessage;
        }
    }
}
