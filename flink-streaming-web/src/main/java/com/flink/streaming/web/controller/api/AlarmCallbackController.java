package com.flink.streaming.web.controller.api;

import com.flink.streaming.web.ao.JobServerAO;
import com.flink.streaming.web.common.RestResult;
import com.flink.streaming.web.controller.web.BaseController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zhuhuipei
 * @Description:
 * @date 2020-07-07
 * @time 22:00
 */
@RestController
@Slf4j
public class AlarmCallbackController extends BaseController {

  @Autowired
  private JobServerAO jobYarnServerAO;

  @RequestMapping("/ok")
  public RestResult ok() {
    return RestResult.success();
  }

  @PostMapping("/alarmCallback")
  public RestResult alarmCallback(@RequestParam String appId, @RequestParam String jobName, @RequestParam String deployMode) {
    log.info("测试回调 appId={} jobName={} deployMode={}", appId, jobName, deployMode);
    return RestResult.success();
  }

}
