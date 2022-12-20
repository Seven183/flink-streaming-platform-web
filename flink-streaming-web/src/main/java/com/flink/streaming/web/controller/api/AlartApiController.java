package com.flink.streaming.web.controller.api;

import com.flink.streaming.web.common.RestResult;
import com.flink.streaming.web.controller.web.BaseController;
import com.flink.streaming.web.enums.SysConfigEnumType;
import com.flink.streaming.web.model.dto.AlartLogDTO;
import com.flink.streaming.web.model.dto.PageModel;
import com.flink.streaming.web.model.param.AlartLogParam;
import com.flink.streaming.web.model.vo.PageVO;
import com.flink.streaming.web.model.vo.SystemConfigVO;
import com.flink.streaming.web.service.AlartLogService;
import com.flink.streaming.web.service.SystemConfigService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@Slf4j
public class AlartApiController extends BaseController {

    @Autowired
    private SystemConfigService systemConfigService;

    @Autowired
    private AlartLogService alartLogService;

    @RequestMapping(value = "/alartLogList")
    public RestResult<?> queryAlartLogList(ModelMap modelMap, AlartLogParam alartLogParam) {
        if (alartLogParam == null) {
            alartLogParam = new AlartLogParam();
        }
        PageModel<AlartLogDTO> pageModel = alartLogService.queryAlartLog(alartLogParam);
        PageVO<PageModel<AlartLogDTO>> pageVO = new PageVO<PageModel<AlartLogDTO>>();
        pageVO.setPageNum(pageModel.getPageNum());
        pageVO.setPages(pageModel.getPages());
        pageVO.setPageSize(pageModel.getPageSize());
        pageVO.setTotal(pageModel.getTotal());
        pageVO.setData(pageModel);
        return RestResult.success(pageVO);
    }

    @RequestMapping(value = "/alartConfig")
    public RestResult<?> alartConfig(ModelMap modelMap) {
        List<SystemConfigVO> list = SystemConfigVO
                .toListVO(systemConfigService.getSystemConfig(SysConfigEnumType.ALART));
        return RestResult.success(list);
    }

    @RequestMapping("/logErrorInfo")
    public RestResult<?> logErrorInfo(Long id) {
        AlartLogDTO alartLogDTO = alartLogService.findLogById(id);
        if (alartLogDTO == null || StringUtils.isEmpty(alartLogDTO.getFailLog())) {
            return RestResult.error("没有异常数据");
        }
        return RestResult.success(alartLogDTO.getFailLog());

    }
}
