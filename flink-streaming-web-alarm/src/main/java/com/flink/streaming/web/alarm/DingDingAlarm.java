package com.flink.streaming.web.alarm;

public interface DingDingAlarm {

    boolean send(String url, String content);

}
