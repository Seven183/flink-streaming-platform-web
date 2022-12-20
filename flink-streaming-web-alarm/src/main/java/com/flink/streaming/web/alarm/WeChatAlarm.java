package com.flink.streaming.web.alarm;

public interface WeChatAlarm {

    boolean send(String url, String content);

}
