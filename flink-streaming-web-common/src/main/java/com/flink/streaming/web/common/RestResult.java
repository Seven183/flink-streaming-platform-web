package com.flink.streaming.web.common;

import com.flink.streaming.web.common.util.Pages;
import com.flink.streaming.web.enums.SysErrorEnum;
import lombok.Data;

@Data
public class RestResult<T> {


    public static final String SUCCESS = "200";

    private String code = SUCCESS;

    private boolean success;

    private String message;

    private T data;

    private Pages page;

    public RestResult() {
    }

    public static <T> RestResult<T> newInstance(String code, String message, T data, Pages page) {
        RestResult<T> result = new RestResult<T>();
        result.code = code;
        result.success = (code.equalsIgnoreCase(SUCCESS));
        result.message = message;
        result.data = data;
        result.page = page;
        return result;
    }

    public static <T> RestResult<T> newInstance(String code, String message, T data) {
        return newInstance(code, message, data, null);
    }

    public static <T> RestResult<T> success() {
        return newInstance(SUCCESS, "", null);
    }

    public static <T> RestResult<T> success(T data) {
        return newInstance(SUCCESS, "", data);
    }

    public static <T> RestResult<T> error(String code, String message) {
        return newInstance(code, message, null, null);
    }

    public static <T> RestResult<T> error(String message) {
        return newInstance("500", message, null, null);
    }

    public static <T> RestResult<T> error(SysErrorEnum sysErrorEnum) {
        return error(sysErrorEnum.getCode(), sysErrorEnum.getErrorMsg());
    }

}
