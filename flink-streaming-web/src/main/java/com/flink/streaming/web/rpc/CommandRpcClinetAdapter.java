package com.flink.streaming.web.rpc;

import com.flink.streaming.web.enums.DeployModeEnum;

public interface CommandRpcClinetAdapter {

    /**
     * 提交服务
     */
    String submitJob(String command, StringBuilder localLog, Long jobRunLogId,
                     DeployModeEnum deployModeEnum)
            throws Exception;

    /**
     * yarn per模式执行savepoint
     * <p>
     * 默认savepoint保存的地址是：hdfs:///flink/savepoint/flink-streaming-platform-web/
     */
    void savepointForPerYarn(String jobId, String targetDirectory, String yarnAppId) throws Exception;

    /**
     * 集群模式下执行savepoint
     */
    void savepointForPerCluster(String jobId, String targetDirectory) throws Exception;


}
