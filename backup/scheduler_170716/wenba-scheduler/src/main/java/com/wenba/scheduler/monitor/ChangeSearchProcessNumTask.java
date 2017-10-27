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
 * change search process number task
 * 
 * @author zhangbo
 *
 */
@Component("changeSearchProcessNumTask")
public class ChangeSearchProcessNumTask {

    // 成员变量
    private static Logger logger = LogManager
            .getLogger(ChangeSearchProcessNumTask.class);
    @Resource
    private SchedulerConfiguration schedulerConfiguration;
    @Resource
    private SchedulerControllerStatistics schedulerControllerStatistics;

    // 每15秒触发一次
    @Scheduled(cron = "0/15 * * * * *")
    public void execute() {
        if (SchedulerStrategy.POLLING.equals(schedulerConfiguration
                .getSystemDataConfiguration().getSchedulerStrategy())
                && schedulerConfiguration.getValidSearchServersSize() > 0) {
            int maxSearchProcessNum = schedulerControllerStatistics
                    .getCurrentSearchRequestNum()
                    / schedulerConfiguration.getValidSearchServersSize();
            if (maxSearchProcessNum > schedulerConfiguration
                    .getSystemDataConfiguration().getSearchProcessNumThres()) {
                schedulerConfiguration.getSystemDataConfiguration()
                        .setMaxSearchProcessNum(maxSearchProcessNum);
                logger.info("current max search process num: {}",
                        schedulerConfiguration.getSystemDataConfiguration()
                                .getMaxSearchProcessNum());
            } else {
                schedulerConfiguration.getSystemDataConfiguration()
                        .setMaxSearchProcessNum(
                                schedulerConfiguration
                                        .getSystemDataConfiguration()
                                        .getSearchProcessNumThres());
            }
        }
    }
}
