package com.wenba.scheduler.monitor;

import javax.annotation.Resource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.wenba.scheduler.SchedulerControllerStatistics;
import com.wenba.scheduler.config.SchedulerConfiguration;
import com.wenba.scheduler.config.SystemDataConfiguration.SchedulerStrategy;

/**
 * change cnn process number task
 * 
 * @author zhangbo
 *
 */
@Component("changeCnnProcessNumTask")
public class ChangeCnnProcessNumTask {

    // 成员变量
    private static Logger logger = LogManager
            .getLogger(ChangeCnnProcessNumTask.class);
    @Resource
    private SchedulerConfiguration schedulerConfiguration;
    @Resource
    private SchedulerControllerStatistics schedulerControllerStatistics;

    // 每15秒触发一次
    @Scheduled(cron = "0/15 * * * * *")
    public void execute() {
        if (SchedulerStrategy.POLLING.equals(schedulerConfiguration
                .getSystemDataConfiguration().getSchedulerStrategy())
                && schedulerConfiguration.getValidCnnServersSize() > 0) {
            int maxCnnProcessNum = schedulerControllerStatistics
                    .getCurrentRequestNum()
                    / schedulerConfiguration.getValidCnnServersSize();
            if (maxCnnProcessNum > schedulerConfiguration
                    .getSystemDataConfiguration().getCnnProcessNumThres()) {
                if (maxCnnProcessNum > schedulerConfiguration
                        .getSystemDataConfiguration()
                        .getCnnProcessMaxNumThres()) {
                    schedulerConfiguration.getSystemDataConfiguration()
                            .setMaxCnnProcessNum(
                                    schedulerConfiguration
                                            .getSystemDataConfiguration()
                                            .getCnnProcessMaxNumThres());
                } else {
                    schedulerConfiguration.getSystemDataConfiguration()
                            .setMaxCnnProcessNum(maxCnnProcessNum);
                }
                logger.info("current max cnn process num: {}",
                        schedulerConfiguration.getSystemDataConfiguration()
                                .getMaxCnnProcessNum());
            } else {
                schedulerConfiguration.getSystemDataConfiguration()
                        .setMaxCnnProcessNum(
                                schedulerConfiguration
                                        .getSystemDataConfiguration()
                                        .getCnnProcessNumThres());
            }
        }
    }
}
