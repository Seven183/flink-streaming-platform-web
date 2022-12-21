package com.flink.streaming.web.enums;

import lombok.Getter;


@Getter
public enum AlarmTypeEnum {
    DINGDING(1, "钉钉告警"),
    CALLBACK_URL(2, "回调http告警"),
    WECHAT(3, "企业微信告警"),
    AUTO_START_JOB(4, "任务退出自动拉起");


    private int code;

    private String desc;

    AlarmTypeEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static AlarmTypeEnum getAlarmTypeEnum(Integer code) {
        if (code == null) {
            return null;
        }

        for (AlarmTypeEnum alarMLogTypeEnum : AlarmTypeEnum.values()) {
            if (alarMLogTypeEnum.getCode() == code.intValue()) {
                return alarMLogTypeEnum;
            }

        }
        return null;
    }
}
