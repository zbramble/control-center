package com.wenba.scheduler.monitor;

import java.text.SimpleDateFormat;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * collect garbage task
 * 
 * @author zhangbo
 *
 */
@Component("collectGCTask")
public class CollectGCTask {

    // 成员变量
    private static Logger logger = LogManager.getLogger(CollectGCTask.class);

    // 每天早上7点触发一次
    @Scheduled(cron = "0 0 7 ? * *")
    public void execute() {
        System.gc();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String currentTime = sdf.format(System.currentTimeMillis());
        logger.info("GC exec at {}!", currentTime);
    }
}
