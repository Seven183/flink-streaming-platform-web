package com.flink.streaming.web.service;

import com.flink.streaming.web.model.dto.SavepointBackupDTO;

import java.util.Date;
import java.util.List;

public interface SavepointBackupService {

    /**
     * 新增
     */
    void insertSavepoint(Long jobConfigId, String savepointPath, Date backupTime);

    /**
     * 最近5条
     */
    List<SavepointBackupDTO> lasterHistory10(Long jobConfigId);

    /**
     * 获取SavepointPath详细地址
     */
    String getSavepointPathById(Long jobConfigId, Long id);

}
