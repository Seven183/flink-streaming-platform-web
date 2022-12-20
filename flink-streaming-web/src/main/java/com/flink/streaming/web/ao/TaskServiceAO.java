package com.flink.streaming.web.ao;

public interface TaskServiceAO {

    /**
     * 检查任务状态一致性问题（任务是不是挂掉）
     * <p>
     * 如：数据库里面任务状态是运行中，实际任务在集群上已经挂掉啦
     */
    void checkJobStatus();

    /**
     * 检查yarn上任务存在 但是状态已经停止，这个时候停止Yarn任务
     */
    @Deprecated
    void checkYarnJobByStop();

    /**
     * 针对在线运行任务进行自动SavePoint
     */
    void autoSavePoint();
}
