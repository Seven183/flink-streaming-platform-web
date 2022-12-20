package com.flink.streaming.web.controller.api;

import com.flink.streaming.web.ao.AlarmServiceAO;
import com.flink.streaming.web.ao.JobServerAO;
import com.flink.streaming.web.ao.WeChatService;
import com.flink.streaming.web.common.RestResult;
import com.flink.streaming.web.common.SystemConstants;
import com.flink.streaming.web.controller.web.BaseController;
import com.flink.streaming.web.enums.DeployModeEnum;
import com.flink.streaming.web.enums.SysConfigEnum;
import com.flink.streaming.web.model.dto.JobConfigDTO;
import com.flink.streaming.web.model.vo.CallbackDTO;
import com.flink.streaming.web.service.SystemConfigService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api")
@RestController
@Slf4j
public class AlarmCallbackController extends BaseController {

    @Autowired
    private JobServerAO jobYarnServerAO;

    @Autowired
    private WeChatService weChatService;

    @Autowired
    private AlarmServiceAO alarmServiceAO;

    @Autowired
    private SystemConfigService systemConfigService;

    @PostMapping("/ok")
    public RestResult<?> ok() {
        String cause = "yyy";
        JobConfigDTO jobConfigDTO = new JobConfigDTO();
        jobConfigDTO.setDeployModeEnum(DeployModeEnum.STANDALONE);
        jobConfigDTO.setId(98L);
        jobConfigDTO.setJobId("jdajsdkajkdakd");
        jobConfigDTO.setJobName("test");
        weChatService.doAlarmNotify(jobConfigDTO, cause);
        return RestResult.success("测试成功");
    }

    @PostMapping("/alarmCallback")
    public RestResult<?> alarmCallback(@RequestParam String appId, @RequestParam String jobName, @RequestParam String deployMode) {
        log.info("测试回调 appId={} jobName={} deployMode={}", appId, jobName, deployMode);
        return RestResult.success();
    }

    @RequestMapping("/testDingdingAlert")
    public RestResult<?> testDingdingAlert() {
        try {
            String alartUrl = systemConfigService.getSystemConfigByKey(SysConfigEnum.DINGDING_ALARM_URL.getKey());
            if (StringUtils.isEmpty(alartUrl)) {
                return RestResult.error("钉钉告警地址不存在");
            }
            boolean isSuccess = alarmServiceAO.sendForDingding(alartUrl,
                    SystemConstants.buildDingdingMessage("测试钉钉"), 0L);
            if (isSuccess) {
                return RestResult.success();
            }
        } catch (Exception e) {
            log.error("testDingdingAlert is fail", e);
        }

        return RestResult.error("钉钉告警测试失败");
    }

    @RequestMapping("/testEnterpriseWeChatAlert")
    public RestResult<?> testEnterpriseWeChatAlert() {
        try {
            String weChatUrl = systemConfigService.getSystemConfigByKey(SysConfigEnum.ENTERPRISEWECHAT_ALARM_URL.getKey());
            if (StringUtils.isEmpty(weChatUrl)) {
                return RestResult.error("EnterpriseWeChatURL地址不存在");
            }
            boolean isSuccess = alarmServiceAO.sendForWeChat(weChatUrl,
                    SystemConstants.buildWeChatMessage("测试企业微信"), 0L);

            if (isSuccess) {
                return RestResult.success();
            }
            return RestResult.error("测试失败fdf");
        } catch (Exception e) {
            log.error("testEnterpriseWeChatAlert is fail", e);
        }

        return RestResult.error("企业微信测试失败");
    }

    @RequestMapping("/testHttpAlert")
    public RestResult<?> testHttpAlert() {
        try {
            String callbackUrl = systemConfigService.getSystemConfigByKey(SysConfigEnum.CALLBACK_ALARM_URL.getKey());
            if (StringUtils.isEmpty(callbackUrl)) {
                return RestResult.error("回调URL地址不存在");
            }
            CallbackDTO callbackDTO = new CallbackDTO();
            callbackDTO.setAppId("测试AppId");
            callbackDTO.setDeployMode("测试DeployMode");
            callbackDTO.setJobName("测试JobName");
            callbackDTO.setJobConfigId(0L);
            boolean isSuccess = alarmServiceAO.sendForHttp(callbackUrl, callbackDTO);
            if (isSuccess) {
                return RestResult.success();
            }
            return RestResult.error("测试失败");
        } catch (Exception e) {
            log.error("testHttpAlert is fail", e);
        }

        return RestResult.error("自定义回调测试失败");
    }

}
