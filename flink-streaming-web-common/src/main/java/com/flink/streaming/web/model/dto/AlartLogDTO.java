package com.flink.streaming.web.model.dto;

import cn.hutool.core.collection.CollectionUtil;
import com.flink.streaming.web.enums.AlarmLogStatusEnum;
import com.flink.streaming.web.enums.AlarmLogTypeEnum;
import com.flink.streaming.web.model.entity.AlartLog;
import lombok.Data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * @author
 */
@Data
public class AlartLogDTO {

    private Long id;

    private Long jobConfigId;

    private String jobName;

    private String message;

    private AlarmLogTypeEnum alarMLogTypeEnum;

    private AlarmLogStatusEnum alarmLogStatusEnum;

    private String failLog;

    private Date createTime;

    private Date editTime;

    private String creator;

    private String editor;


    public static AlartLog toEntity(AlartLogDTO alartLogDTO) {
        if (alartLogDTO == null) {
            return null;
        }
        AlartLog alartLog = new AlartLog();
        alartLog.setId(alartLogDTO.getId());
        alartLog.setJobConfigId(alartLogDTO.getJobConfigId());
        alartLog.setMessage(alartLogDTO.getMessage());
        alartLog.setType(alartLogDTO.getAlarMLogTypeEnum().getCode());
        alartLog.setStatus(alartLogDTO.getAlarmLogStatusEnum().getCode());
        alartLog.setFailLog(alartLogDTO.getFailLog());
        alartLog.setCreateTime(alartLogDTO.getCreateTime());
        alartLog.setEditTime(alartLogDTO.getEditTime());
        alartLog.setCreator(alartLogDTO.getCreator());
        alartLog.setEditor(alartLogDTO.getEditor());
        alartLog.setJobName(alartLogDTO.getJobName());
        return alartLog;
    }

    public static AlartLogDTO toDTO(AlartLog alartLog) {
        if (alartLog == null) {
            return null;
        }
        AlartLogDTO alartLogDTO = new AlartLogDTO();
        alartLogDTO.setId(alartLog.getId());
        alartLogDTO.setJobConfigId(alartLog.getJobConfigId());
        alartLogDTO.setMessage(alartLog.getMessage());
        alartLogDTO.setAlarMLogTypeEnum(AlarmLogTypeEnum.getAlarmLogTypeEnum(alartLog.getType()));
        alartLogDTO
                .setAlarmLogStatusEnum(AlarmLogStatusEnum.getAlarmLogStatusEnum(alartLog.getStatus()));
        alartLogDTO.setFailLog(alartLog.getFailLog());
        alartLogDTO.setCreateTime(alartLog.getCreateTime());
        alartLogDTO.setEditTime(alartLog.getEditTime());
        alartLogDTO.setCreator(alartLog.getCreator());
        alartLogDTO.setEditor(alartLog.getEditor());
        alartLogDTO.setJobName(alartLog.getJobName());
        return alartLogDTO;
    }

    public static List<AlartLogDTO> toListDTO(List<AlartLog> alartLogList) {
        if (CollectionUtil.isEmpty(alartLogList)) {
            return Collections.emptyList();
        }
        List<AlartLogDTO> list = new ArrayList<>();
        for (AlartLog alartLog : alartLogList) {
            AlartLogDTO alartLogDTO = AlartLogDTO.toDTO(alartLog);
            if (alartLog != null) {
                list.add(alartLogDTO);
            }
        }
        return list;
    }
}
