package com.flink.streaming.web.model.vo;

import com.flink.streaming.web.model.dto.JobConfigDTO;
import lombok.Data;

@Data
public class CallbackDTO {

    private String appId;

    private String jobName;

    private String deployMode;

    private Long jobConfigId;


    public static CallbackDTO to(JobConfigDTO jobConfigDTO) {
        CallbackDTO callbackDTO = new CallbackDTO();
        callbackDTO.setJobConfigId(jobConfigDTO.getId());
        callbackDTO.setJobName(jobConfigDTO.getJobName());
        callbackDTO.setDeployMode(jobConfigDTO.getDeployModeEnum().name());
        callbackDTO.setAppId(jobConfigDTO.getJobId());
        return callbackDTO;
    }
}
