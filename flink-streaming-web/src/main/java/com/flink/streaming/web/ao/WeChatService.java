package com.flink.streaming.web.ao;

import com.flink.streaming.web.model.dto.JobConfigDTO;

public interface WeChatService {

    /**
     * 定制化告警通知
     */
    void doAlarmNotify(JobConfigDTO jobConfigDTO, String cause);

}
