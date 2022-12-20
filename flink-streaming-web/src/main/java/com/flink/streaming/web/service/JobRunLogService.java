package com.flink.streaming.web.service;

import com.flink.streaming.web.model.dto.JobRunLogDTO;
import com.flink.streaming.web.model.dto.PageModel;
import com.flink.streaming.web.model.param.JobRunLogParam;


public interface JobRunLogService {

    /**
     * 新增任务 返回主键
     */
    Long insertJobRunLog(JobRunLogDTO jobRunLogDTO);

    /**
     * 日志更新
     */
    void updateLogById(String log, Long id);

    /**
     * 更新
     */
    void updateJobRunLogById(JobRunLogDTO jobRunLogDTO);

    /**
     * 日志功能查询
     */
    PageModel<JobRunLogDTO> queryJobRunLog(JobRunLogParam jobRunLogParam);

    /**
     * 单个日志详情
     */
    JobRunLogDTO getDetailLogById(Long id);

    /**
     * 删除日志
     */
    void deleteLogByJobConfigId(Long jobConfigId);


}
