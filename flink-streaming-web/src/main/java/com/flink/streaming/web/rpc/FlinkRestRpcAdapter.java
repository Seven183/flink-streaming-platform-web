package com.flink.streaming.web.rpc;

import com.flink.streaming.web.enums.DeployModeEnum;
import com.flink.streaming.web.rpc.model.JobStandaloneInfo;

public interface FlinkRestRpcAdapter {

    /**
     * Standalone 模式下获取状态
     */
    JobStandaloneInfo getJobInfoForStandaloneByAppId(String appId, DeployModeEnum deployModeEnum);

    /**
     * 基于flink rest API取消任务
     */
    void cancelJobForFlinkByAppId(String jobId, DeployModeEnum deployModeEnum);

    /**
     * 获取savepoint路径
     */
    String savepointPath(String jobId, DeployModeEnum deployModeEnum);

}
