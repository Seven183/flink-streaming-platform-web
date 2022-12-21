package com.flink.streaming.web.service;

import com.flink.streaming.web.enums.JobConfigStatus;
import com.flink.streaming.web.enums.YN;
import com.flink.streaming.web.model.dto.JobConfigDTO;
import com.flink.streaming.web.model.dto.PageModel;
import com.flink.streaming.web.model.entity.BatchJob;
import com.flink.streaming.web.model.param.JobConfigParam;

import java.util.List;

public interface JobConfigService {

    /**
     * 新增配置 返回主键Id
     */
    Long addJobConfig(JobConfigDTO jobConfigDTO);

    /**
     * 修改配置
     */
    void updateJobConfigById(JobConfigDTO jobConfigDTO);

    /**
     * 修改配置(记录历史信息)
     */
    void updateJobConfigByIdWithWriteHistory(JobConfigDTO jobConfigDTO);

    void updateJobConfigStatusById(Long id, JobConfigStatus jobConfigStatus);

    /**
     * 启动状态更新 有乐观锁
     */
    void updateStatusByStart(Long id, String userName, Long jobRunLogId, Integer version);

    /**
     * 单个查询任务详情
     */
    JobConfigDTO getJobConfigById(Long id);

    /**
     * 单个查询任务详情(包括删除)
     */
    JobConfigDTO getJobConfigByIdContainDelete(Long id);

    /**
     * 开启或者配置
     */
    void openOrClose(Long id, YN yn, String userName);

    /**
     * 删除任务
     */
    void deleteJobConfigById(Long id, String userName);

    /**
     * 恢复删除任务
     */
    int recoveryDeleteJobConfigById(Long id, String userName);

    /**
     * 分页查询
     */
    PageModel<JobConfigDTO> queryJobConfig(JobConfigParam jobConfigParam);

    /**
     * 按状态获取任务
     */
    List<JobConfigDTO> findJobConfigByStatus(Integer... status);

    /**
     * 获取有效的批任务
     */
    List<BatchJob> getAllBatchJobs();
}
