package com.flink.streaming.web.controller.api;

import com.flink.streaming.web.common.RestResult;
import com.flink.streaming.web.controller.web.BaseController;
import com.flink.streaming.web.enums.SysConfigEnumType;
import com.flink.streaming.web.exceptions.BizException;
import com.flink.streaming.web.model.vo.SystemConfigVO;
import com.flink.streaming.web.service.SystemConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@Slf4j
public class SysConfigApiController extends BaseController {


    @Autowired
    private SystemConfigService systemConfigService;

    @PostMapping(value = "/upsertSynConfig")
    public RestResult<?> upsertSynConfig(String key, String val) {
        try {
            systemConfigService.addOrUpdateConfigByKey(key, val.trim());
        } catch (BizException biz) {
            log.warn("upsertSynConfig is error ", biz);
            return RestResult.error(biz.getMessage());
        } catch (Exception e) {
            log.error("upsertSynConfig is error", e);
            return RestResult.error(e.getMessage());
        }
        return RestResult.success();
    }


    @PostMapping(value = "/deleteConfig")
    public RestResult<?> deleteConfig(String key) {
        try {
            systemConfigService.deleteConfigByKey(key);
        } catch (BizException biz) {
            log.warn("upsertSynConfig is error ", biz);
            return RestResult.error(biz.getMessage());
        } catch (Exception e) {
            log.error("upsertSynConfig is error", e);
            return RestResult.error(e.getMessage());
        }
        return RestResult.success();
    }

    @RequestMapping(value = "/sysConfig")
    public RestResult<?> sysConfig(ModelMap modelMap) {
        List<SystemConfigVO> list = SystemConfigVO
                .toListVO(systemConfigService.getSystemConfig(SysConfigEnumType.SYS));
        return RestResult.success(list);
    }

}
