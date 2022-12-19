package com.flink.streaming.web.ao.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.flink.streaming.web.ao.JobConfigAO;
import com.flink.streaming.web.common.FlinkStandaloneRestUriConstants;
import com.flink.streaming.web.common.util.HttpUtil;
import com.flink.streaming.web.model.dto.JobConfigDTO;
import com.flink.streaming.web.service.JobAlarmConfigService;
import com.flink.streaming.web.service.JobConfigService;
import com.flink.streaming.web.service.SystemConfigService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author zhuhuipei
 * @Description:
 * @date 2021/2/28
 * @time 11:25
 */
@Component
@Slf4j
public class JobConfigAOImpl implements JobConfigAO {

    @Autowired
    private JobConfigService jobConfigService;

    @Autowired
    private JobAlarmConfigService jobAlarmConfigService;

    @Autowired
    private SystemConfigService systemConfigService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addJobConfig(JobConfigDTO jobConfigDTO) {
        if (StringUtils.isBlank(jobConfigDTO.getFlinkCheckpointConfig())) {
            String flinkHttpAddress = systemConfigService.getFlinkHttpAddress(jobConfigDTO.getDeployModeEnum());
            String uriJobsForStandalone = FlinkStandaloneRestUriConstants.getUriJobsForStandalone();
            String url = HttpUtil.buildUrl(flinkHttpAddress, uriJobsForStandalone);
            String res = HttpUtil.buildRestTemplate(HttpUtil.TIME_OUT_1_M).getForObject(url, String.class);

            JSONArray objects = JSONArray.parseArray(res);
            for (int i = 0; i <= objects.size() - 1; i++) {
                JSONObject jsonObject = JSONObject.parseObject(objects.get(i).toString());
                if (jsonObject.get("key").equals("state.checkpoints.dir")) {
                    jobConfigDTO.setFlinkCheckpointConfig("-checkpointDir " + jsonObject.get("value").toString());
                }
            }
        }

        Long jobConfigId = jobConfigService.addJobConfig(jobConfigDTO);
        jobAlarmConfigService
                .upSertBatchJobAlarmConfig(jobConfigDTO.getAlarmTypeEnumList(), jobConfigId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateJobConfigById(JobConfigDTO jobConfigDTO) {
        jobConfigService.updateJobConfigByIdWithWriteHistory(jobConfigDTO);
        jobAlarmConfigService
                .upSertBatchJobAlarmConfig(jobConfigDTO.getAlarmTypeEnumList(), jobConfigDTO.getId());
    }
}
