package com.flink.streaming.web.service;

import com.flink.streaming.web.model.dto.JobConfigHistoryDTO;
import com.flink.streaming.web.model.dto.PageModel;
import com.flink.streaming.web.model.param.JobConfigHisotryParam;

import java.util.List;

public interface JobConfigHistoryService {

    /**
     * 新增记录
     */
    void insertJobConfigHistory(JobConfigHistoryDTO jobConfigHistoryDTO);

    /**
     * 查询历史记录
     */
    List<JobConfigHistoryDTO> getJobConfigHistoryByJobConfigId(Long jobConfigId);

    /**
     * 详情
     */
    JobConfigHistoryDTO getJobConfigHistoryById(Long id);

    /**
     * 分页查询
     */
    PageModel<JobConfigHistoryDTO> queryJobConfigHistory(JobConfigHisotryParam jobConfigParam);

}
