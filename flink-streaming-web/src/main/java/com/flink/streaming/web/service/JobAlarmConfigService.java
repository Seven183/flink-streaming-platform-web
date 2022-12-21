package com.flink.streaming.web.service;

import com.flink.streaming.web.enums.AlarmTypeEnum;

import java.util.List;
import java.util.Map;

public interface JobAlarmConfigService {

    /**
     * 批量新增/修改
     */
    void upSertBatchJobAlarmConfig(List<AlarmTypeEnum> alarmTypeEnumList, Long jobId);


    /**
     * 按jobId查询
     */
    List<AlarmTypeEnum> findByJobId(Long jobId);


    /**
     * @author zhuhuipei
     */
    Map<Long, List<AlarmTypeEnum>> findByJobIdList(List<Long> jobIdList);

}
