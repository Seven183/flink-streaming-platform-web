package com.flink.streaming.web.ao;

import com.flink.streaming.web.model.dto.JobConfigDTO;

public interface JobConfigAO {

    /**
     * 新增
     */
    void addJobConfig(JobConfigDTO jobConfigDTO);

    /**
     * 修改参数
     */
    void updateJobConfigById(JobConfigDTO jobConfigDTO);
}
