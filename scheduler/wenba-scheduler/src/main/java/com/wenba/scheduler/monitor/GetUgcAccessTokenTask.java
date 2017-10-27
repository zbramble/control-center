package com.wenba.scheduler.monitor;

import java.text.SimpleDateFormat;

import javax.annotation.Resource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.wenba.scheduler.config.SchedulerConfiguration;
import com.wenba.scheduler.ugc.UgcCommonStrategy;
import com.wenba.scheduler.ugc.UgcParam;

/**
 * collect garbage task
 * 
 * @author zhangbo
 *
 */
@Component("getUgcAccessTokenTask")
public class GetUgcAccessTokenTask {

    // 成员变量
    private static Logger logger = LogManager
            .getLogger(GetUgcAccessTokenTask.class);
    @Resource
    private SchedulerConfiguration schedulerConfiguration;
    @Resource
    private UgcCommonStrategy ugcCommonStrategy;

    // 每小时触发一次
    @Scheduled(cron = "0 0 * * * *")
    public void execute() {
        // 获取ugc access token start
        UgcParam ugcCommonParam = new UgcParam();
        ugcCommonParam.setSchedulerConfiguration(schedulerConfiguration);
        ugcCommonStrategy.getAccessToken(ugcCommonParam);
        // 获取ugc access token end

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String currentTime = sdf.format(System.currentTimeMillis());
        logger.info(
                "GetUgcAccessTokenTask exec at {}, current access token: {}",
                currentTime, schedulerConfiguration.getUgcConfigConfiguration()
                        .getAccessToken());
    }
}
