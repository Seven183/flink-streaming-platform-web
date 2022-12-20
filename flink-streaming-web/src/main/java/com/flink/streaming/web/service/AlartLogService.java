package com.flink.streaming.web.service;

import com.flink.streaming.web.model.dto.AlartLogDTO;
import com.flink.streaming.web.model.dto.PageModel;
import com.flink.streaming.web.model.param.AlartLogParam;

public interface AlartLogService {

    void addAlartLog(AlartLogDTO alartLogDTO);

    AlartLogDTO findLogById(Long id);

    PageModel<AlartLogDTO> queryAlartLog(AlartLogParam alartLogParam);

}
