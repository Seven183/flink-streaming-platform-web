package com.flink.streaming.web.ao;

import com.flink.streaming.web.model.dto.JobConfigDTO;
import com.flink.streaming.web.model.dto.JobRunParamDTO;

public interface JobBaseServiceAO {

    /**
     * 提交任务前校验数据
     */
    void checkStart(JobConfigDTO jobConfigDTO);

    /**
     * Savepoint前校验数据
     */
    void checkSavepoint(JobConfigDTO jobConfigDTO);

    /**
     * 管配置检查
     */
    void checkClose(JobConfigDTO jobConfigDTO);

    Long insertJobRunLog(JobConfigDTO jobConfigDTO, String userName);

    /**
     * 将配置的sql 写入文件并且返回运行所需参数
     */
    JobRunParamDTO writeSqlToFile(JobConfigDTO jobConfigDTO);


    /**
     * 异步执行任务
     */
    void aSyncExecJob(
            final JobRunParamDTO jobRunParamDTO,
            final JobConfigDTO jobConfig,
            final Long jobRunLogId,
            final String savepointPath);
}
