package com.flink.streaming.web.scheduler;

import com.flink.streaming.web.ao.TaskServiceAO;
import com.flink.streaming.web.quartz.BatchJobManagerScheduler;
import com.flink.streaming.web.service.IpStatusService;
import com.flink.streaming.web.service.SystemConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Configurable
@EnableScheduling
@EnableAsync
public class SchedulerTask {


    @Autowired
    private IpStatusService ipStatusService;

    @Autowired
    private TaskServiceAO taskServiceAO;

    @Autowired
    private SystemConfigService systemConfigService;

    @Autowired
    private BatchJobManagerScheduler batchJobManagerScheduler;


    /**
     * 每个1分钟更新一次心跳时间
     */
    @Async("taskExecutor")
    @Scheduled(cron = "0 */1 * * * ?")
    public void checkHeartbeat() {
        log.debug("#####心跳检查checkHeartbeat#######");
        try {
            ipStatusService.updateHeartbeatBylocalIp();
        } catch (Exception e) {
            log.error("心跳检查失败", e);
        }
    }


    /**
     * 每隔5分钟进行一次一致性校验检查(如果校验失败会进行告警)
     */
    @Async("taskExecutor")
    @Scheduled(cron = "0 */1 * * * ?")
    public void checkJobStatus() {
        if (!ipStatusService.isLeader()) {
            return;
        }
        log.info("#####[task-start]一致性校验检查#######");
        try {
            taskServiceAO.checkJobStatus();
        } catch (Exception e) {
            log.error("checkJobStatusByYarn is error", e);
        }
        log.info("#####[task-end]一致性校验检查#######");
    }

//    /**
//     * 每隔20分钟进行一次对停止任务进行是否在yarn上运行的检查
//     *
//     * @author zhuhuipei
//     * @date 2020-10-25
//     * @time 18:34
//     */
//    @Async("taskExecutor")
//    @Scheduled(cron = "0 */20 * * * ?")
//    public void checkYarnJobByStop() {
//        if (!ipStatusService.isLeader()) {
//            return;
//        }
//        log.info("#####checkYarnJobByStop#######");
//        try {
//            taskServiceAO.checkYarnJobByStop();
//        } catch (Exception e) {
//            log.error("checkYarnJobByStop is error", e);
//        }
//    }


    /**
     * 每隔30分钟进行一次自动savePoint
     */
    @Async("taskExecutor")
    @Scheduled(cron = "0 */30 * * * ?")
    //@Scheduled(cron = "0 */1 * * * ?")
    public void autoSavePoint() {
        if (!systemConfigService.autoSavepoint()) {
            log.info("#####没有开启自动savePoint#######");
            return;
        }

        if (!ipStatusService.isLeader()) {
            return;
        }
        log.info("#####[task]开始自动执行savePoint#######");
        try {
            taskServiceAO.autoSavePoint();
        } catch (Exception e) {
            log.error("autoSavePoint is error", e);
        }
    }

    /**
     * 定时检测离线任务调度注册情况（补偿）
     */
    @Async("taskExecutor")
    @Scheduled(cron = "0 */30 * * * ?")
    //@Scheduled(cron = "0 */1 * * * ?")
    public void autoBatchRegisterJob() {
        if (!ipStatusService.isLeader()) {
            return;
        }
        log.info("#####定时检测离线任务调度注册情况（半个小时一次检测）#######");
        try {
            batchJobManagerScheduler.batchRegisterJob();
        } catch (Exception e) {
            log.error("autoSavePoint is error", e);
        }
    }


}
