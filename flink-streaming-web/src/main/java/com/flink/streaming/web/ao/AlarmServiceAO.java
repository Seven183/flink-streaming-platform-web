package com.flink.streaming.web.ao;

import com.flink.streaming.web.model.vo.CallbackDTO;


public interface AlarmServiceAO {

    /**
     * 发送钉钉告警消息
     */
    boolean sendForDingding(String url, String content, Long jobConfigId);

    /**
     * 发送企业微信告警消息
     */
    boolean sendForWeChat(String url, String content, Long jobConfigId);

    /**
     * 发送http请求回调
     */
    boolean sendForHttp(String url, CallbackDTO callbackDTO);
}
