package com.flink.streaming.web.controller.api;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.flink.streaming.common.constant.SystemConstant;
import com.flink.streaming.common.enums.JobTypeEnum;
import com.flink.streaming.common.model.CheckPointParam;
import com.flink.streaming.web.ao.JobConfigAO;
import com.flink.streaming.web.ao.JobServerAO;
import com.flink.streaming.web.common.*;
import com.flink.streaming.web.common.util.CliConfigUtil;
import com.flink.streaming.web.common.util.HttpServiceCheckerUtil;
import com.flink.streaming.web.common.util.HttpUtil;
import com.flink.streaming.web.controller.web.BaseController;
import com.flink.streaming.web.enums.*;
import com.flink.streaming.web.exceptions.BizException;
import com.flink.streaming.web.factory.JobServerAOFactory;
import com.flink.streaming.web.model.dto.JobConfigDTO;
import com.flink.streaming.web.model.dto.JobConfigHistoryDTO;
import com.flink.streaming.web.model.dto.PageModel;
import com.flink.streaming.web.model.dto.SavepointBackupDTO;
import com.flink.streaming.web.model.param.JobConfigHisotryParam;
import com.flink.streaming.web.model.param.JobConfigParam;
import com.flink.streaming.web.model.param.UpsertJobConfigParam;
import com.flink.streaming.web.model.vo.DeployFlinkVO;
import com.flink.streaming.web.model.vo.DeployFlinkVO.FlinkTask;
import com.flink.streaming.web.model.vo.PageVO;
import com.flink.streaming.web.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.flink.shaded.jackson2.org.yaml.snakeyaml.Yaml;
import org.quartz.CronExpression;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@Slf4j
public class JobConfigApiController extends BaseController {

    @Autowired
    private JobConfigService jobConfigService;

    @Autowired
    private JobConfigAO jobConfigAO;

    @Autowired
    private JobConfigHistoryService jobConfigHistoryService;

    @Autowired
    private SystemConfigService systemConfigService;

    @Autowired
    private JobAlarmConfigService jobAlarmConfigService;

    @Autowired
    private SavepointBackupService savepointBackupService;

    @RequestMapping("/start")
    public RestResult<String> start(Long id, Long savepointId) {
        try {
            this.getJobServerAO(id).start(id, savepointId, this.getUserName());
        } catch (BizException e) {
            log.error("启动失败 id={}", id, e);
            return RestResult.error(e.getCode(), e.getErrorMsg());
        } catch (Exception e) {
            log.error("启动失败 id={}", id, e);
            return RestResult.error(SysErrorEnum.START_JOB_FAIL);
        }
        return RestResult.success();
    }

    @RequestMapping("/stop")
    public RestResult<String> stop(Long id) {
        try {
            this.getJobServerAO(id).stop(id, this.getUserName());
        } catch (BizException e) {
            log.warn("停止失败 id={}", id, e);
            return RestResult.error(e.getCode(), e.getErrorMsg());
        } catch (Exception e) {
            log.error("停止失败 id={}", id, e);
            return RestResult.error(SysErrorEnum.STOP_JOB_FAIL);
        }
        return RestResult.success();
    }

    @RequestMapping("/close")
    public RestResult<String> close(Long id) {
        try {
            this.getJobServerAO(id).close(id, this.getUserName());
        } catch (BizException e) {
            log.warn("关闭失败 id={}", id, e);
            return RestResult.error(e.getCode(), e.getErrorMsg());
        } catch (Exception e) {
            log.error("关闭失败 id={}", id, e);
            return RestResult.error(SysErrorEnum.START_JOB_FAIL);
        }
        return RestResult.success();
    }

    @RequestMapping("/open")
    public RestResult<String> open(Long id) {
        try {
            this.getJobServerAO(id).open(id, this.getUserName());
        } catch (BizException e) {
            log.warn("开始失败 id={}", id, e);
            return RestResult.error(e.getCode(), e.getErrorMsg());
        } catch (Exception e) {
            log.error("开始失败 id={}", id, e);
            return RestResult.error(SysErrorEnum.START_JOB_FAIL);
        }
        return RestResult.success();
    }

    @RequestMapping("/delete")
    public RestResult<String> delete(Long id) {
        try {
            jobConfigService.deleteJobConfigById(id, this.getUserName());
        } catch (BizException e) {
            log.warn("删除失败 id={}", id, e);
            return RestResult.error(e.getCode(), e.getErrorMsg());
        } catch (Exception e) {
            log.error("删除失败 id={}", id, e);
            return RestResult.error(SysErrorEnum.START_JOB_FAIL);
        }
        return RestResult.success();
    }

    @RequestMapping("/savepoint")
    public RestResult<String> savepoint(Long id) {
        try {
            this.getJobServerAO(id).savepoint(id);
        } catch (BizException e) {
            log.warn("savepoint is error id={}", id, e);
            return RestResult.error(e.getCode(), e.getErrorMsg());
        } catch (Exception e) {
            log.error("savepoint is error id={}", id, e);
            return RestResult.error(SysErrorEnum.SAVEPOINT_JOB_FAIL);
        }
        return RestResult.success();
    }

    @PostMapping(value = "/addConfig")
    public RestResult<?> addConfig(UpsertJobConfigParam upsertJobConfigParam) {

        try {
            RestResult restResult = checkUpsertJobConfigParam(upsertJobConfigParam);
            if (restResult != null) {
                return restResult;
            }
            jobConfigAO.addJobConfig(UpsertJobConfigParam.toDTO(upsertJobConfigParam));
        } catch (BizException biz) {
            log.warn("addJobConfig is error ", biz);
            return RestResult.error(biz.getErrorMsg());
        } catch (Exception e) {
            log.error("addJobConfig is error", e);
            return RestResult.error(e.getMessage());
        }
        return RestResult.success();
    }

    @PostMapping(value = "/editConfig")
    public RestResult<?> editConfig(UpsertJobConfigParam upsertJobConfigParam) {
        try {
            RestResult<?> restResult = checkUpsertJobConfigParam(upsertJobConfigParam);
            if (restResult != null) {
                return restResult;
            }
            JobConfigDTO jobConfigDTO = jobConfigService.getJobConfigById(upsertJobConfigParam.getId());
            if (jobConfigDTO == null) {
                return RestResult.error("数据不存在");
            }

            completeJObConfigDTO(jobConfigDTO);

            if (YN.getYNByValue(jobConfigDTO.getIsOpen()).getCode()) {
                return RestResult.error(SysErrorEnum.JOB_CONFIG_JOB_IS_OPEN.getErrorMsg());
            }
            JobConfigDTO jobConfigNew = UpsertJobConfigParam.toDTO(upsertJobConfigParam);

            jobConfigAO.updateJobConfigById(jobConfigNew);
        } catch (BizException biz) {
            log.warn("updateJobConfigById is error ", biz);
            return RestResult.error(biz.getErrorMsg());
        } catch (Exception e) {
            log.error("updateJobConfigById is error", e);
            return RestResult.error(e.getMessage());
        }
        return RestResult.success();
    }

    @PostMapping(value = "/copyConfig")
    public RestResult copyConfig(UpsertJobConfigParam upsertJobConfigParam) {
        try {
            JobConfigDTO jobConfigDTO = jobConfigService.getJobConfigById(upsertJobConfigParam.getId());
            if (jobConfigDTO == null) {
                return RestResult.error("原始拷贝数据不存在");
            }
            /*
             * copy job conf 默认将id去除
             * 默认在任务名称后面copy_随机字符_${jobConfigDTO.getJobName()}字符 状态默认重置为停止 开启配置
             * isOpen 0
             */
            jobConfigDTO.setId(null);
            jobConfigDTO.setJobName(
                    String
                            .format("copy_%s_%s", StringUtils.lowerCase(RandomStringUtils.randomAlphanumeric(4)),
                                    jobConfigDTO.getJobName()));
            jobConfigDTO.setStatus(JobConfigStatus.STOP);
            jobConfigDTO.setIsOpen(YN.N.getValue());
            jobConfigDTO.setJobId(null);
            jobConfigDTO.setLastRunLogId(null);
            jobConfigDTO.setVersion(0);
            jobConfigDTO.setLastStartTime(null);
            jobConfigDTO.setLastRunLogId(null);

            jobConfigAO.addJobConfig(jobConfigDTO);

        } catch (BizException biz) {
            log.warn("copyJobConfigById is error ", biz);
            return RestResult.error(biz.getErrorMsg());
        } catch (Exception e) {
            log.error("copyJobConfigById is error", e);
            return RestResult.error(e.getMessage());
        }
        return RestResult.success();
    }

    /**
     * 查询作业列表
     */
    @PostMapping(value = "/listTask")
    public RestResult<?> listTask(ModelMap modelMap, JobConfigParam jobConfigParam) {
        if (jobConfigParam == null) {
            jobConfigParam = new JobConfigParam();
        }
        PageModel<JobConfigDTO> pageModel = jobConfigService.queryJobConfig(jobConfigParam);
        completeJObConfigDTO(pageModel);
        PageVO pageVO = new PageVO();
        pageVO.setPageNum(pageModel.getPageNum());
        pageVO.setPages(pageModel.getPages());
        pageVO.setPageSize(pageModel.getPageSize());
        pageVO.setTotal(pageModel.getTotal());
        pageVO.setData(pageModel);
        return RestResult.success(pageVO);
    }

    /**
     * 查询历史版本
     */
    @RequestMapping(value = "/jobConfigHistoryPage")
    public RestResult<?> listPage(ModelMap modelMap, JobConfigHisotryParam jobConfigParam) {
        PageModel<JobConfigHistoryDTO> pageModel = jobConfigHistoryService
                .queryJobConfigHistory(jobConfigParam);
        PageVO pageVO = new PageVO();
        pageVO.setPageNum(pageModel.getPageNum());
        pageVO.setPages(pageModel.getPages());
        pageVO.setPageSize(pageModel.getPageSize());
        pageVO.setTotal(pageModel.getTotal());
        pageVO.setData(pageModel);
        return RestResult.success(pageVO);
    }

    /**
     * 查询历史版本详情
     */
    @RequestMapping("/jobConfigHistoryDetail")
    public RestResult<?> detailPage(ModelMap modelMap, Long id) {
        JobConfigHistoryDTO jobConfigHistoryDTO = jobConfigHistoryService.getJobConfigHistoryById(id);
        return RestResult.success(jobConfigHistoryDTO);
    }

    /**
     * 从代码库中发布代码到Flink管理平台
     *
     * @param deployConfigFile 配置文件
     * @param deployPath       发布代码根路径
     * @param deployUser       发布用户
     * @param versionDesc      发布版本说明
     * @return
     * @throws Exception
     * @author wxj
     * @date 2021年12月27日 下午4:11:34
     * @version V1.0
     */
    @RequestMapping("/deployFlinkTask")
    public RestResult<?> deployFlinkTask(String deployConfigFile, String deployPath,
                                         String deployUser, String versionDesc) throws Exception {
        Yaml yaml = new Yaml();
        try (InputStream in = new FileInputStream(deployConfigFile)) {
            DeployFlinkVO deploy = yaml.loadAs(in, DeployFlinkVO.class);
            int count = 0;
            for (FlinkTask task : deploy.getTaskList()) {
                if (task.getId() == null) {
                    continue;
                }
                count++;
                String jobDesc =
                        (StringUtils.isBlank(task.getJobDesc()) ? "" : task.getJobDesc() + "-") + versionDesc;
                if (jobDesc.length() > 100) {
                    jobDesc = jobDesc.substring(0, 100);
                }
                JobTypeEnum jobType =
                        task.getJobType() == null ? JobTypeEnum.SQL_STREAMING : task.getJobType();
                DeployModeEnum deployMode =
                        task.getDeployMode() == null ? DeployModeEnum.STANDALONE : task.getDeployMode();
                String flinkSql = StringUtils.isBlank(task.getSqlFile()) ? null
                        : this.readTextFile(deployPath + SystemConstant.VIRGULE + task.getSqlFile());
                String alarmTypes = getAlarmTypes(task.getAlarmTypes());
                String flinkRunConfig = task.getFlinkRunConfig();
                UpsertJobConfigParam jobConfigParam = new UpsertJobConfigParam();
                jobConfigParam.setId(task.getId());
                jobConfigParam.setJobType(jobType.getCode());
                jobConfigParam.setJobName(task.getJobName());
                jobConfigParam.setJobDesc(jobDesc);
                jobConfigParam.setDeployMode(deployMode.name());
                jobConfigParam.setFlinkRunConfig(flinkRunConfig);
                jobConfigParam.setFlinkCheckpointConfig(task.getFlinkCheckpointConfig());
                jobConfigParam.setFlinkSql(flinkSql);
                jobConfigParam.setAlarmTypes(alarmTypes);
                jobConfigParam.setExtJarPath(task.getExtJarPath());
                jobConfigParam.setCustomArgs(task.getCustomArgs());
                jobConfigParam.setCustomMainClass(task.getCustomMainClass());
                jobConfigParam.setCustomJarUrl(task.getCustomJarUrl());

                JobConfigDTO job = jobConfigService.getJobConfigByIdContainDelete(task.getId());
                if (job != null) { // 更新
                    if (jobType != job.getJobTypeEnum()) {
                        throw new BizException("不能变更编号为[" + task.getId() + "]的任务类型！");
                    }
                    jobConfigParam.setIsOpen(job.getIsOpen());
                    jobConfigParam.setStatus(job.getStatus().getCode());
                    JobConfigDTO updateJobConfig = UpsertJobConfigParam.toDTO(jobConfigParam);
                    if (job.getIsDeleted() == 1) { // 已经打删除标记的任务重新启用
                        jobConfigService.recoveryDeleteJobConfigById(job.getId(), deployUser);
                    }
                    jobConfigAO.updateJobConfigById(updateJobConfig);
                } else { // 新增
                    jobConfigParam.setIsOpen(1);
                    jobConfigParam.setStatus(JobConfigStatus.SUCCESS.getCode());
                    jobConfigParam.setFlinkRunConfig(flinkRunConfig == null ? "" : flinkRunConfig);
                    JobConfigDTO addJobConfig = UpsertJobConfigParam.toDTO(jobConfigParam);
                    jobConfigAO.addJobConfig(addJobConfig);
                }
                log.info("[{}]发布任务：[{}]{},{}", deployUser, jobConfigParam.getId(),
                        jobConfigParam.getJobName(), versionDesc);
            }
            // 重新启动任务
            for (FlinkTask task : deploy.getTaskList()) {
                if (task.getId() == null) {
                    continue;
                }
                if (task.getDeployStartFlag() != null && !task.getDeployStartFlag()) {
                    continue;
                }
                JobConfigDTO job = jobConfigService.getJobConfigById(task.getId());
                if (job.getIsOpen() != 1) {
                    continue;
                }
                // 停止任务
                if (StringUtils.isNotBlank(job.getJobId())) {
                    this.getJobServerAO(job.getId()).stop(job.getId(), deployUser);
                }
                // 查询最近一次的SavePoint，恢复运行
                List<SavepointBackupDTO> savepointBackupDTOList = savepointBackupService
                        .lasterHistory10(job.getId());
                Long savepointId = (savepointBackupDTOList != null && savepointBackupDTOList.size() > 0)
                        ? savepointBackupDTOList.get(0).getId() : null;
                this.getJobServerAO(job.getId()).start(job.getId(), savepointId, deployUser);
            }
            return RestResult.success("成功发布" + count + "个任务！");
        } catch (Exception e) {
            log.error("发布失败!", e);
            return RestResult.error(e.getMessage());
        }
    }

    private String getAlarmTypes(List<AlarmTypeEnum> list) {
        if (list == null || list.size() == 0) {
            return null;
        }
        String result = "";
        for (AlarmTypeEnum alarmTypeEnum : list) {
            result += (result.length() > 0 ? "," : "") + alarmTypeEnum.getCode();
        }
        return result;
    }

    private String readTextFile(String fileName) {
        try (InputStream fin = new FileInputStream(fileName);) {
            byte[] buffer = new byte[fin.available()];
            fin.read(buffer);
            fin.close();
            String result = new String(buffer, SystemConstants.CODE_UTF_8);
            return result;
        } catch (Exception e) {
            log.error("读取文件[" + fileName + "]失败！", e);
        }
        return null;
    }

    //CHECKSTYLE:OFF
    private RestResult checkUpsertJobConfigParam(UpsertJobConfigParam upsertJobConfigParam) {
        if (upsertJobConfigParam == null) {
            return RestResult.error("参数不能空");
        }
        if (StringUtils.isEmpty(upsertJobConfigParam.getJobName())) {
            return RestResult.error("任务名称不能空");
        }
        if (upsertJobConfigParam.getJobName().length() > 50) {
            return RestResult.error("任务名称不能超过50个字符");
        }
        if (!upsertJobConfigParam.getJobName().matches("[0-9A-Za-z_]*")) {
            return RestResult.error("任务名称仅能含数字,字母和下划线");
        }

        // jar需要校验参数
        if (JobTypeEnum.JAR.getCode() == upsertJobConfigParam.getJobType()) {

            if (StringUtils.isEmpty(upsertJobConfigParam.getCustomMainClass())) {
                return RestResult.error("主类不能为空");
            }

            if (StringUtils.isEmpty(upsertJobConfigParam.getCustomJarUrl())) {
                return RestResult.error("主类jar的不能为空");
            }
        }
        // sql配置需要校验的参数JobType=null是兼容之前配置
        if (upsertJobConfigParam.getJobType() == null
                || JobTypeEnum.SQL_STREAMING.getCode() == upsertJobConfigParam.getJobType()) {
            if (StringUtils.isEmpty(upsertJobConfigParam.getFlinkSql())) {
                return RestResult.error("sql语句不能为空");
            }
            if (StringUtils.isNotEmpty(upsertJobConfigParam.getExtJarPath())) {
                String[] urls = upsertJobConfigParam.getExtJarPath().split(SystemConstant.LINE_FEED);
                for (String url : urls) {
                    if (StringUtils.isEmpty(url)) {
                        continue;
                    }
                }
            }
        }
        if (JobTypeEnum.SQL_BATCH.getCode() == upsertJobConfigParam.getJobType() && StringUtils
                .isNotEmpty(upsertJobConfigParam.getCron())) {
            if (!CronExpression.isValidExpression(upsertJobConfigParam.getCron())) {
                return RestResult.error("cron表达式不准确");
            }
        }

        if (StringUtils.isNotEmpty(upsertJobConfigParam.getFlinkCheckpointConfig())) {
            CheckPointParam checkPointParam = CliConfigUtil
                    .checkFlinkCheckPoint(upsertJobConfigParam.getFlinkCheckpointConfig());
            RestResult restResult = this.checkPointParam(checkPointParam);
            if (restResult != null && !restResult.isSuccess()) {
                return restResult;
            }
        } else {

            String targetDirectory = SystemConstants.DEFAULT_SAVEPOINT_ROOT_PATH;
            String flinkHttpAddress = systemConfigService.getSystemConfigByKey(SysConfigEnum.FLINK_REST_HTTP_ADDRESS.getKey());
            String uriJobsForStandalone = FlinkStandaloneRestUriConstants.getUriJobsForStandalone();
            String url = HttpUtil.buildUrl(flinkHttpAddress, uriJobsForStandalone);
            String res = HttpUtil.buildRestTemplate(HttpUtil.TIME_OUT_1_M).getForObject(url, String.class);

            JSONArray objects = JSONArray.parseArray(res);
            for (int i = 0; i <= objects.size() - 1; i++) {
                JSONObject jsonObject = JSONObject.parseObject(objects.get(i).toString());
                if (jsonObject.get("key").equals("state.savepoints.dir")) {
                    targetDirectory = "-checkpointDir " + jsonObject.get("value").toString();
                    upsertJobConfigParam.setFlinkCheckpointConfig(targetDirectory);
                }
            }
        }

        if (DeployModeEnum.YARN_PER.name().equals(upsertJobConfigParam.getDeployMode()) ||
                DeployModeEnum.YARN_APPLICATION.name().equals(upsertJobConfigParam.getDeployMode())) {
            if (StringUtils.isEmpty(upsertJobConfigParam.getFlinkRunConfig())) {
                return RestResult.error("flink运行配置不能为空");
            }
            if (upsertJobConfigParam.getFlinkRunConfig().contains("-Dyarn.application.name=")) {
                return RestResult.error("请不要配置-Dyarn.application.name= 参数 ");
            }
        }

        return null;
    }
//CHECKSTYLE:ON

    /**
     * 获取JobServerAO
     */
    private JobServerAO getJobServerAO(Long id) {
        JobConfigDTO jobConfigDTO = jobConfigService.getJobConfigById(id);
        if (jobConfigDTO == null) {
            throw new BizException(SysErrorEnum.JOB_CONFIG_JOB_IS_NOT_EXIST);
        }
        return JobServerAOFactory.getJobServerAO(jobConfigDTO.getDeployModeEnum());
    }

    private RestResult checkPointParam(CheckPointParam checkPointParam) {
        if (checkPointParam == null) {
            return RestResult.success();
        }
        if (StringUtils.isNotEmpty(checkPointParam.getCheckpointingMode())) {
            if (!(FlinkConstants.EXACTLY_ONCE.equalsIgnoreCase(checkPointParam.getCheckpointingMode())
                    || FlinkConstants.AT_LEAST_ONCE
                    .equalsIgnoreCase(checkPointParam.getCheckpointingMode()))) {
                return RestResult.error("checkpointingMode 参数必须是  AT_LEAST_ONCE 或者 EXACTLY_ONCE");
            }
        }
        if (StringUtils.isNotEmpty(checkPointParam.getExternalizedCheckpointCleanup())) {
            if (!(FlinkConstants.DELETE_ON_CANCELLATION
                    .equalsIgnoreCase(checkPointParam.getExternalizedCheckpointCleanup())
                    || FlinkConstants.RETAIN_ON_CANCELLATION
                    .equalsIgnoreCase(checkPointParam.getExternalizedCheckpointCleanup()))) {
                return RestResult.error(
                        "externalizedCheckpointCleanup 参数必须是DELETE_ON_CANCELLATION 或者 RETAIN_ON_CANCELLATION");
            }
        }
        return RestResult.success();
    }


    /**
     * 补充字段信息
     */
    private void completeJObConfigDTO(JobConfigDTO jobConfigDTO) {
        Map<DeployModeEnum, String> domainKey = new HashMap<>();
        domainKey.put(DeployModeEnum.YARN_PER,
                systemConfigService.getSystemConfigByKey(SysConfigEnum.YARN_RM_HTTP_ADDRESS.getKey()));
        domainKey.put(DeployModeEnum.YARN_APPLICATION,
                systemConfigService.getSystemConfigByKey(SysConfigEnum.YARN_RM_HTTP_ADDRESS.getKey()));
        domainKey.put(DeployModeEnum.LOCAL,
                systemConfigService.getSystemConfigByKey(SysConfigEnum.FLINK_REST_HTTP_ADDRESS.getKey()));
        domainKey.put(DeployModeEnum.STANDALONE, systemConfigService
                .getSystemConfigByKey(SysConfigEnum.FLINK_REST_HA_HTTP_ADDRESS.getKey()));
        // 补充FlinkRunUrl字段
        String domain = domainKey.get(jobConfigDTO.getDeployModeEnum());
        if (StringUtils.isNotEmpty(domain)) {
            if ((DeployModeEnum.YARN_PER.equals(jobConfigDTO.getDeployModeEnum())
                    || DeployModeEnum.YARN_APPLICATION.equals(jobConfigDTO.getDeployModeEnum()))
                    && !StringUtils
                    .isEmpty(jobConfigDTO.getJobId())) {
                jobConfigDTO.setFlinkRunUrl(HttpUtil.buildUrl(domain,
                        FlinkYarnRestUriConstants.getUriOverviewForYarn(jobConfigDTO.getJobId())));
            }
            if (DeployModeEnum.LOCAL.equals(jobConfigDTO.getDeployModeEnum()) && !StringUtils
                    .isEmpty(jobConfigDTO.getJobId())) {
                jobConfigDTO.setFlinkRunUrl(domain + String
                        .format(FlinkYarnRestUriConstants.URI_YARN_JOB_OVERVIEW, jobConfigDTO.getJobId()));
            }
            if (DeployModeEnum.STANDALONE.equals(jobConfigDTO.getDeployModeEnum()) && !StringUtils
                    .isEmpty(jobConfigDTO.getJobId())) {
                String[] urls = domain.split(SystemConstant.SEMICOLON);
                for (String url : urls) {
                    if (HttpServiceCheckerUtil.checkUrlConnect(url)) {
                        jobConfigDTO.setFlinkRunUrl(url.trim() + String
                                .format(FlinkYarnRestUriConstants.URI_YARN_JOB_OVERVIEW, jobConfigDTO.getJobId()));
                        break;
                    }
                }
            }
        }
        // 补充AlarmStrs字段
        List<AlarmTypeEnum> list = jobAlarmConfigService.findByJobId(jobConfigDTO.getId());
        if (CollectionUtil.isNotEmpty(list)) {
            List<Integer> alarmTypes = new ArrayList<Integer>();
            StringBuilder str = new StringBuilder("[");
            for (AlarmTypeEnum alarmTypeEnum : list) {
                alarmTypes.add(alarmTypeEnum.getCode());
                if (str.length() > 1) {
                    str.append(", ");
                }
                switch (alarmTypeEnum) {
                    case DINGDING:
                        str.append("钉钉");
                        break;
                    case CALLBACK_URL:
                        str.append("http回调");
                        break;
                    case WECHAT:
                        str.append("企业微信");
                        break;
                    case AUTO_START_JOB:
                        str.append("自动重启");
                        break;
                    default:
                }
            }
            str.append("]");
            jobConfigDTO.setAlarmStrs(str.toString());
            jobConfigDTO.setAlarmTypes(alarmTypes);
            jobConfigDTO.setAlarmTypeEnumList(list);
        }
    }

    /**
     * 补充字段信息
     */
    private void completeJObConfigDTO(PageModel<JobConfigDTO> pageModel) {
        if (pageModel == null || pageModel.size() == 0) {
            return;
        }
        List<Long> jobIdList = pageModel.stream().map(jobConfigVO -> jobConfigVO.getId())
                .collect(Collectors.toList());
        Map<Long, List<AlarmTypeEnum>> map = jobAlarmConfigService.findByJobIdList(jobIdList);

        for (JobConfigDTO jobConfigDTO : pageModel) {
            // 补充FlinkRunUrl字段
            String domain = systemConfigService.getFlinkUrl(jobConfigDTO.getDeployModeEnum());

            if (StringUtils.isNotEmpty(domain)) {
                if ((DeployModeEnum.YARN_PER.equals(jobConfigDTO.getDeployModeEnum())
                        || DeployModeEnum.YARN_APPLICATION.equals(jobConfigDTO.getDeployModeEnum())
                        && !StringUtils.isEmpty(jobConfigDTO.getJobId()))) {
                    jobConfigDTO.setFlinkRunUrl(HttpUtil.buildUrl(domain,
                            FlinkYarnRestUriConstants.getUriOverviewForYarn(jobConfigDTO.getJobId())));
                }
                if ((DeployModeEnum.LOCAL.equals(jobConfigDTO.getDeployModeEnum())
                        || DeployModeEnum.STANDALONE.equals(jobConfigDTO.getDeployModeEnum()))
                        && !StringUtils.isEmpty(jobConfigDTO.getJobId())) {
                    jobConfigDTO.setFlinkRunUrl(domain + String
                            .format(FlinkYarnRestUriConstants.URI_YARN_JOB_OVERVIEW, jobConfigDTO.getJobId()));
                }
            }
            // 补充AlarmStrs字段
            List<AlarmTypeEnum> list = map.get(jobConfigDTO.getId());
            if (CollectionUtil.isNotEmpty(list)) {
                List<Integer> alarmTypes = new ArrayList<Integer>();
                StringBuilder str = new StringBuilder("[");
                for (AlarmTypeEnum alarmTypeEnum : list) {
                    alarmTypes.add(alarmTypeEnum.getCode());
                    if (str.length() > 1) {
                        str.append(", ");
                    }
                    switch (alarmTypeEnum) {
                        case DINGDING:
                            str.append("钉钉");
                            break;
                        case CALLBACK_URL:
                            str.append("http回调");
                            break;
                        case WECHAT:
                            str.append("企业微信");
                            break;
                        case AUTO_START_JOB:
                            str.append("自动重启");
                            break;
                        default:
                    }
                }
                str.append("]");
                jobConfigDTO.setAlarmStrs(str.toString());
                jobConfigDTO.setAlarmTypes(alarmTypes);
                jobConfigDTO.setAlarmTypeEnumList(list);
            }
        }
    }

}
