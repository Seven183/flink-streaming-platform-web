package com.flink.streaming.web.utils;

import com.flink.streaming.web.common.RestResult;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@Slf4j
public class WebUtil {


    /**
     * 判断是否为Ajax请求
     */
    public static boolean isAjaxRequest(HttpServletRequest request) {
        return "XMLHttpRequest".equalsIgnoreCase(request.getHeader("X-Requested-With"));
    }

    /**
     * 响应修改为Restful风格返回Json
     */
    public static void restResponse(HttpServletResponse response, String status, String message) {
        RestResult<Object> respone = RestResult.newInstance(status, message, null);
        restResponse(response, respone);
    }

    /**
     * 响应修改为Restful风格返回Json
     */
    public static void restResponseWithFlush(HttpServletResponse response, String status,
                                             String message) {
        restResponse(response, status, message);
        flush(response);
    }

    /**
     * 响应修改为Restful风格返回Json
     */
    public static void restResponse(HttpServletResponse response, String status, String message,
                                    Object data) {
        RestResult<Object> respone = RestResult.newInstance(status, message, data);
        restResponse(response, respone);
    }

    /**
     * 响应修改为Restful风格返回Json
     */
    public static void restResponseWithFlush(HttpServletResponse response, String status,
                                             String message, Object data) {
        restResponse(response, status, message, data);
        flush(response);
    }

    /**
     * 响应修改为Restful风格返回Json
     */
    public static void restResponse(HttpServletResponse response, RestResult<?> data) {
        try {
            response.setContentType("application/json;charset=utf-8");
            PrintWriter out = response.getWriter();
            out.write(JacksonUtil.toJsonString(data));
            out.flush();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * 响应修改为Restful风格返回Json
     */
    public static void restResponseWithFlush(HttpServletResponse response, RestResult<?> data) {
        restResponse(response, data);
        flush(response);
    }

    /**
     * Flush Response，设置完成状态
     */
    public static void flush(HttpServletResponse response) {
        try {
            response.getWriter().flush();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }
}
