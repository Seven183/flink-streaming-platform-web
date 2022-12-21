package com.flink.streaming.web.rpc;

import com.flink.streaming.web.enums.YarnStateEnum;
import com.flink.streaming.web.rpc.model.JobInfo;

public interface YarnRestRpcAdapter {

    /**
     * 通过任务名称获取yarn 的appId
     */
    String getAppIdByYarn(String jobName, String queueName);

    /**
     * 通过http杀掉一个任务
     */
    void stopJobByJobId(String appId);

    /**
     * 查询yarn 上某任务状态
     */
    YarnStateEnum getJobStateByJobId(String appId);

    /**
     * per yarn 模式下获取任务新状态
     */
    JobInfo getJobInfoForPerYarnByAppId(String appId);

    /**
     * per yarn 模式下 取消任务
     */
    void cancelJobForYarnByAppId(String appId, String jobId);

    /**
     * per yarn 模式下 获取SavepointPath 地址
     * <p>
     * 通过checkpoint 接口获取Savepoint地址
     */
    String getSavepointPath(String appId, String jobId);

}
