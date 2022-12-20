package com.flink.streaming.web.alarm;

import com.flink.streaming.web.model.vo.CallbackDTO;

public interface HttpAlarm {

    boolean send(String url, CallbackDTO callbackDTO);
}
