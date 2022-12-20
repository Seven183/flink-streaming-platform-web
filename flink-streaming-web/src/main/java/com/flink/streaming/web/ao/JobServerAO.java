package com.flink.streaming.web.ao;

public interface JobServerAO {

    /**
     * 启动任务
     */
    void start(Long id, Long savepointId, String userName);

    /**
     * 关闭任务
     */
    void stop(Long id, String userName);

    /**
     * 执行savepoint
     */
    void savepoint(Long id);

    /**
     * 开启配置
     */
    void open(Long id, String userName);

    /**
     * 关闭配置
     */
    void close(Long id, String userName);
}
