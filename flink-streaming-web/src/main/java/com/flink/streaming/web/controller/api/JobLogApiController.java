package com.flink.streaming.web.controller.api;

import com.flink.streaming.web.common.RestResult;
import com.flink.streaming.web.config.CustomConfig;
import com.flink.streaming.web.controller.web.BaseController;
import com.flink.streaming.web.enums.YN;
import com.flink.streaming.web.model.dto.JobRunLogDTO;
import com.flink.streaming.web.model.dto.PageModel;
import com.flink.streaming.web.model.param.JobRunLogParam;
import com.flink.streaming.web.model.vo.JobRunLogVO;
import com.flink.streaming.web.model.vo.PageVO;
import com.flink.streaming.web.service.JobRunLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 日志API
 */
@RestController
@RequestMapping("/api")
public class JobLogApiController extends BaseController {

    @Autowired
    private JobRunLogService jobRunLogService;

    @Autowired
    private CustomConfig customConfig;

    @PostMapping(value = "/logList")
    public RestResult<?> listTask(JobRunLogParam jobRunLogParam) {
        PageModel<JobRunLogDTO> pageModel = jobRunLogService.queryJobRunLog(jobRunLogParam);
        PageVO<PageModel<JobRunLogDTO>> pageVO = new PageVO<PageModel<JobRunLogDTO>>();
        pageVO.setPageNum(pageModel.getPageNum());
        pageVO.setPages(pageModel.getPages());
        pageVO.setPageSize(pageModel.getPageSize());
        pageVO.setTotal(pageModel.getTotal());
        pageVO.setData(pageModel);
        return RestResult.success(pageVO);
    }

    @PostMapping(value = "/logDetail")
    public RestResult<?> sysConfig(ModelMap modelMap, Long logid) {
        JobRunLogVO vo = JobRunLogVO
                .toVO(jobRunLogService.getDetailLogById(logid), YN.Y.getCode(), customConfig.getWebPort());
        return RestResult.success(vo);
    }
}
