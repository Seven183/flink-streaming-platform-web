package com.flink.streaming.web.ao.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.flink.streaming.common.enums.JobTypeEnum;
import com.flink.streaming.web.ao.JobBaseServiceAO;
import com.flink.streaming.web.ao.JobServerAO;
import com.flink.streaming.web.ao.WeChatService;
import com.flink.streaming.web.common.FlinkStandaloneRestUriConstants;
import com.flink.streaming.web.common.MessageConstants;
import com.flink.streaming.web.common.SystemConstants;
import com.flink.streaming.web.common.util.HttpUtil;
import com.flink.streaming.web.enums.*;
import com.flink.streaming.web.exceptions.BizException;
import com.flink.streaming.web.model.dto.JobConfigDTO;
import com.flink.streaming.web.model.dto.JobRunParamDTO;
import com.flink.streaming.web.model.entity.BatchJob;
import com.flink.streaming.web.quartz.BatchJobManagerScheduler;
import com.flink.streaming.web.rpc.CommandRpcClinetAdapter;
import com.flink.streaming.web.rpc.FlinkRestRpcAdapter;
import com.flink.streaming.web.rpc.model.JobStandaloneInfo;
import com.flink.streaming.web.service.JobConfigService;
import com.flink.streaming.web.service.SavepointBackupService;
import com.flink.streaming.web.service.SystemConfigService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Map;

@Component(SystemConstants.BEANNAME_JOBSTANDALONESERVERAO)
@Slf4j
public class JobStandaloneServerAOImpl implements JobServerAO {

    @Autowired
    private JobConfigService jobConfigService;

    @Autowired
    private SavepointBackupService savepointBackupService;

    @Autowired
    private CommandRpcClinetAdapter commandRpcClinetAdapter;

    @Autowired
    private FlinkRestRpcAdapter flinkRestRpcAdapter;

    @Autowired
    private JobBaseServiceAO jobBaseServiceAO;

    @Autowired
    private BatchJobManagerScheduler batchJobRegister;

    @Autowired
    private SystemConfigService systemConfigService;

    @Autowired
    private WeChatService weChatService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void start(Long id, Long savepointId, String userName) {

        JobConfigDTO jobConfigDTO = jobConfigService.getJobConfigById(id);

        if (StringUtils.isNotBlank(jobConfigDTO.getJobId())) {
            if (!jobConfigDTO.getJobTypeEnum().equals(JobTypeEnum.SQL_BATCH)) {
                JobStandaloneInfo jobStatus = flinkRestRpcAdapter.getJobInfoForStandaloneByAppId(
                        jobConfigDTO.getJobId(),
                        jobConfigDTO.getDeployModeEnum());
                if (StringUtils.isNotBlank(jobStatus.getState()) && SystemConstants.STATUS_RUNNING.equalsIgnoreCase(jobStatus.getState())) {
                    throw new BizException(
                            "请检查Flink任务列表，任务ID=[" + jobConfigDTO.getJobId() + "]处于[ " + jobStatus.getState() + "]状态，不能重复启动任务！");
                }
            }
        }

        //1、检查jobConfigDTO 状态等参数
        jobBaseServiceAO.checkStart(jobConfigDTO);

        //2、将配置的sql 写入本地文件并且返回运行所需参数
        JobRunParamDTO jobRunParamDTO = jobBaseServiceAO.writeSqlToFile(jobConfigDTO);

        //3、插一条运行日志数据
        Long jobRunLogId = jobBaseServiceAO.insertJobRunLog(jobConfigDTO, userName);

        //4、变更任务状态（变更为：启动中） 有乐观锁 防止重复提交
        jobConfigService.updateStatusByStart(jobConfigDTO.getId(), userName, jobRunLogId, jobConfigDTO.getVersion());
        String savepointPath = savepointBackupService.getSavepointPathById(id, savepointId);

        //异步提交任务
        jobBaseServiceAO.aSyncExecJob(jobRunParamDTO, jobConfigDTO, jobRunLogId, savepointPath);
    }


    @Override
    public void stop(Long id, String userName) {
        log.info("[{}]开始停止任务[{}]", userName, id);
        JobConfigDTO jobConfigDTO = jobConfigService.getJobConfigById(id);
        if (jobConfigDTO == null) {
            throw new BizException(SysErrorEnum.JOB_CONFIG_JOB_IS_NOT_EXIST);
        }
        JobStandaloneInfo jobStandaloneInfo = flinkRestRpcAdapter
                .getJobInfoForStandaloneByAppId(jobConfigDTO.getJobId(), jobConfigDTO.getDeployModeEnum());
        log.info("任务[{}]当前状态为：{}", id, jobStandaloneInfo);
        if (jobStandaloneInfo == null || StringUtils.isNotEmpty(jobStandaloneInfo.getErrors())) {
            log.warn("开始停止任务[{}]，getJobInfoForStandaloneByAppId is error jobStandaloneInfo={}", id, jobStandaloneInfo);
        } else {
            // 停止前先savepoint
            if (StringUtils.isNotBlank(jobConfigDTO.getFlinkCheckpointConfig())
                    && jobConfigDTO.getJobTypeEnum() != JobTypeEnum.SQL_BATCH
                    && SystemConstants.STATUS_RUNNING.equals(jobStandaloneInfo.getState())) {
                log.info("开始保存任务[{}]的状态-savepoint", id);
                this.savepoint(id);
            }
            //停止任务
            if (SystemConstants.STATUS_RUNNING.equals(jobStandaloneInfo.getState())
                    || SystemConstants.STATUS_RESTARTING.equals(jobStandaloneInfo.getState())) {
                flinkRestRpcAdapter
                        .cancelJobForFlinkByAppId(jobConfigDTO.getJobId(), jobConfigDTO.getDeployModeEnum());
            }
        }
        JobConfigDTO jobConfig = new JobConfigDTO();
        jobConfig.setStatus(JobConfigStatus.STOP);
        jobConfig.setEditor(userName);
        jobConfig.setId(id);
        jobConfig.setJobId("");
        //变更状态
        jobConfigService.updateJobConfigById(jobConfig);
    }

    @Override
    public void savepoint(Long id) {
        JobConfigDTO jobConfigDTO = jobConfigService.getJobConfigById(id);

        jobBaseServiceAO.checkSavepoint(jobConfigDTO);

        JobStandaloneInfo jobStandaloneInfo = flinkRestRpcAdapter
                .getJobInfoForStandaloneByAppId(jobConfigDTO.getJobId(), jobConfigDTO.getDeployModeEnum());
        if (jobStandaloneInfo == null || StringUtils.isNotEmpty(jobStandaloneInfo.getErrors())
                || !SystemConstants.STATUS_RUNNING.equals(jobStandaloneInfo.getState())) {
            log.warn(MessageConstants.MESSAGE_007, jobConfigDTO.getJobName());
            weChatService.doAlarmNotify(jobConfigDTO, MessageConstants.MESSAGE_007);
            throw new BizException(MessageConstants.MESSAGE_007);
        }

        //1、 执行savepoint
        try {
            String targetDirectory = SystemConstants.DEFAULT_SAVEPOINT_ROOT_PATH + id;
            String flinkHttpAddress = systemConfigService.getFlinkHttpAddress(jobConfigDTO.getDeployModeEnum());
            String uriJobsForStandalone = FlinkStandaloneRestUriConstants.getUriJobsForStandalone();
            String url = HttpUtil.buildUrl(flinkHttpAddress, uriJobsForStandalone);
            String res = HttpUtil.buildRestTemplate(HttpUtil.TIME_OUT_1_M).getForObject(url, String.class);

            JSONArray objects = JSONArray.parseArray(res);
            for (int i = 0; i <= objects.size() - 1; i++) {
                JSONObject jsonObject = JSONObject.parseObject(objects.get(i).toString());
                if (jsonObject.get("key").equals("state.savepoints.dir")) {
                    targetDirectory = jsonObject.get("value").toString() + "/" + jobConfigDTO.getJobId();
                }
            }
            if (DeployModeEnum.LOCAL.equals(jobConfigDTO.getDeployModeEnum())) {
                targetDirectory = "savepoint/" + id;
            }

            commandRpcClinetAdapter.savepointForPerCluster(jobConfigDTO.getJobId(), targetDirectory);
        } catch (Exception e) {
            log.error(MessageConstants.MESSAGE_008, e);
            weChatService.doAlarmNotify(jobConfigDTO, MessageConstants.MESSAGE_008);
            throw new BizException(MessageConstants.MESSAGE_008);
        }

        String savepointPath = flinkRestRpcAdapter.savepointPath(jobConfigDTO.getJobId(),
                jobConfigDTO.getDeployModeEnum());
        if (StringUtils.isEmpty(savepointPath)) {
            log.warn(MessageConstants.MESSAGE_009, jobConfigDTO);
            weChatService.doAlarmNotify(jobConfigDTO, MessageConstants.MESSAGE_009);
            throw new BizException(MessageConstants.MESSAGE_009);
        }
        //2、 执行保存Savepoint到本地数据库
        savepointBackupService.insertSavepoint(id, savepointPath, new Date());
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void open(Long id, String userName) {
        JobConfigDTO jobConfigDTO = jobConfigService.getJobConfigById(id);
        if (jobConfigDTO == null) {
            return;
        }
        if (jobConfigDTO.getJobTypeEnum() == JobTypeEnum.SQL_BATCH && StringUtils
                .isNotEmpty(jobConfigDTO.getCron())) {
            batchJobRegister
                    .registerJob(new BatchJob(id, jobConfigDTO.getJobName(), jobConfigDTO.getCron()));
        }

        jobConfigService.openOrClose(id, YN.Y, userName);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void close(Long id, String userName) {
        JobConfigDTO jobConfigDTO = jobConfigService.getJobConfigById(id);
        jobBaseServiceAO.checkClose(jobConfigDTO);
        jobConfigService.openOrClose(id, YN.N, userName);
        if (jobConfigDTO.getJobTypeEnum() == JobTypeEnum.SQL_BATCH) {
            batchJobRegister.deleteJob(id);

        }
    }

    private void checkSysConfig(Map<String, String> systemConfigMap, DeployModeEnum deployModeEnum) {
        if (systemConfigMap == null) {
            throw new BizException(SysErrorEnum.SYSTEM_CONFIG_IS_NULL);
        }
        if (!systemConfigMap.containsKey(SysConfigEnum.FLINK_HOME.getKey())) {
            throw new BizException(SysErrorEnum.SYSTEM_CONFIG_IS_NULL_FLINK_HOME);
        }

        if (DeployModeEnum.LOCAL == deployModeEnum
                && !systemConfigMap.containsKey(SysConfigEnum.FLINK_REST_HTTP_ADDRESS.getKey())) {
            throw new BizException(SysErrorEnum.SYSTEM_CONFIG_IS_NULL_FLINK_REST_HTTP_ADDRESS);
        }

        if (DeployModeEnum.STANDALONE == deployModeEnum
                && !systemConfigMap.containsKey(SysConfigEnum.FLINK_REST_HA_HTTP_ADDRESS.getKey())) {
            throw new BizException(SysErrorEnum.SYSTEM_CONFIG_IS_NULL_FLINK_REST_HA_HTTP_ADDRESS);
        }
    }
}
