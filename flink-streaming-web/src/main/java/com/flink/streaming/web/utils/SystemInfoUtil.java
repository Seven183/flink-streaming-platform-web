package com.flink.streaming.web.utils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SystemInfoUtil {

    public static String getEnv(String key) {
        try {
            return System.getenv().get(key);
        } catch (Exception e) {
            log.error("getEnv is error");
        }
        return null;
    }

}
