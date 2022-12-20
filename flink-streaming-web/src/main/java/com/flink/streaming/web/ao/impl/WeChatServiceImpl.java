package com.flink.streaming.web.ao.impl;

import com.flink.streaming.web.alarm.WeChatAlarm;
import com.flink.streaming.web.ao.WeChatService;
import com.flink.streaming.web.enums.AlarmLogStatusEnum;
import com.flink.streaming.web.enums.AlarmLogTypeEnum;
import com.flink.streaming.web.enums.DeployModeEnum;
import com.flink.streaming.web.enums.SysConfigEnum;
import com.flink.streaming.web.exceptions.BizException;
import com.flink.streaming.web.model.dto.AlartLogDTO;
import com.flink.streaming.web.model.dto.JobConfigDTO;
import com.flink.streaming.web.model.dto.JobRunLogDTO;
import com.flink.streaming.web.service.AlartLogService;
import com.flink.streaming.web.service.JobRunLogService;
import com.flink.streaming.web.service.SystemConfigService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class WeChatServiceImpl implements WeChatService {

    @Autowired
    private AlartLogService alartLogService;

    @Autowired
    private JobRunLogService jobRunLogService;

    @Autowired
    private SystemConfigService systemConfigService;

    @Autowired
    private WeChatAlarm weChatAlarm;

    @Override
    public void doAlarmNotify(JobConfigDTO jobConfigDTO, String cause) {

        String content = "【大数据实时计算任务失败】";
        try {
            Long jobConfigId = jobConfigDTO.getId();
            String jobId = jobConfigDTO.getJobId();
            String jobName = jobConfigDTO.getJobName();
            DeployModeEnum deployModeEnum = jobConfigDTO.getDeployModeEnum();
            String flinkRunConfig = jobConfigDTO.getFlinkRunConfig();
            String customArgs = jobConfigDTO.getCustomArgs();
            if (StringUtils.isBlank(jobConfigDTO.getCustomArgs())) {
                customArgs = "";
            }
            if (StringUtils.isBlank(jobConfigDTO.getFlinkRunConfig())) {
                flinkRunConfig = "";
            }
            log.warn("\ncontent: {} \n 原因: {} \n ConfigID: {} \n jobID: {} \n 任务名称: {} \n 运行模式: {} \n 运行参数: {} \n 自定义参数: {} \n ",
                    content, cause, jobConfigId, jobId, jobName, deployModeEnum, flinkRunConfig, customArgs);

            String failLog = "\ncontent: " + content + "\n"
                    + "ConfigID: " + jobConfigId + "\n"
                    + "原因:" + cause + "\n"
                    + "jobID: " + jobId + "\n"
                    + "任务名称: " + jobName + "\n"
                    + "运行模式: " + deployModeEnum + "\n"
                    + "运行参数: " + flinkRunConfig + "\n"
                    + "自定义参数: " + customArgs + "\n";
            String weChatUrl = systemConfigService.getSystemConfigByKey(SysConfigEnum.ENTERPRISEWECHAT_ALARM_URL.getKey());
            weChatAlarm.send(weChatUrl, failLog);

            this.insertLog(false, jobConfigId, failLog, content, AlarmLogTypeEnum.WECHAT_URL);
        } catch (Exception e) {
            throw new BizException("企业微信报警失败");
        }
    }

    private void insertLog(boolean isSuccess, Long jobConfigId, String failLog, String content,
                           AlarmLogTypeEnum alarMLogTypeEnum) {
        JobRunLogDTO jobRunLogDTO = jobRunLogService.getDetailLogById(jobConfigId);
        AlartLogDTO alartLogDTO = new AlartLogDTO();
        alartLogDTO.setJobConfigId(jobConfigId);
        alartLogDTO.setAlarMLogTypeEnum(alarMLogTypeEnum);
        alartLogDTO.setMessage(content);
        alartLogDTO.setFailLog(failLog);
        alartLogDTO.setJobName(jobRunLogDTO.getJobName());
        alartLogDTO
                .setAlarmLogStatusEnum(isSuccess ? AlarmLogStatusEnum.SUCCESS : AlarmLogStatusEnum.FAIL);
        alartLogService.addAlartLog(alartLogDTO);
    }
}
