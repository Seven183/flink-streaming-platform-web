package com.flink.streaming.web.service;

import com.flink.streaming.web.enums.DeployModeEnum;
import com.flink.streaming.web.enums.SysConfigEnumType;
import com.flink.streaming.web.model.dto.SystemConfigDTO;

import java.util.List;

public interface SystemConfigService {

    /**
     * 新增或者修改配置
     */
    void addOrUpdateConfigByKey(String key, String value);

    /**
     * 查询配置
     */
    List<SystemConfigDTO> getSystemConfig(SysConfigEnumType sysConfigEnumType);

    /**
     * 删除一个配置
     */
    void deleteConfigByKey(String key);

    /**
     * 根据key获取配置的值
     */
    String getSystemConfigByKey(String key);

    /**
     * 获取yarn的rm Http地址
     */
    String getYarnRmHttpAddress();

    String getFlinkAddress(DeployModeEnum deployModeEnum);

    /**
     * 获取flink地址
     */
    String getFlinkHttpAddress(DeployModeEnum deployModeEnum);

    /**
     * 获取统一地址
     */
    String getFlinkUrl(DeployModeEnum deployModeEnum);

    /**
     * 上传jar的目录地址
     */
    String getUploadJarsPath();

    /**
     * 检查配置是否存在
     */
    boolean isExist(String key);

    /**
     * 是否自动开启savepoint （默认是true）
     */
    boolean autoSavepoint();

}
