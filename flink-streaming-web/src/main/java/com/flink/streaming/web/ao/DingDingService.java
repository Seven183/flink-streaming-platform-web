package com.flink.streaming.web.ao;

import com.flink.streaming.web.enums.DeployModeEnum;
import com.flink.streaming.web.model.dto.JobConfigDTO;

public interface DingDingService {

    /**
     * 定制化告警通知
     */
    void doAlarmNotify(String cusContent, JobConfigDTO jobConfigDTO, DeployModeEnum deployModeEnum);

}
