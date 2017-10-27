package com.wenba.scheduler.monitor;

import javax.annotation.Resource;

import com.wenba.scheduler.ISchedulerStrategy;
import com.wenba.scheduler.SchedulerControllerStatistics;
import com.wenba.scheduler.config.ConfigParam;
import com.wenba.scheduler.config.ConfigResult;
import com.wenba.scheduler.config.SchedulerConfiguration;
import com.wenba.scheduler.email.EmailParam;

/**
 * collect garbage task
 * 
 * @author zhangbo
 *
 */
// @Component("checkEmailTask")
public class CheckEmailTask {

    // 成员变量
    // private static Logger logger =
    // LogManager.getLogger(CheckEmailTask.class);
    @Resource
    private SchedulerConfiguration schedulerConfiguration;
    @Resource
    private SchedulerControllerStatistics schedulerControllerStatistics;
    @Resource
    private ISchedulerStrategy<ConfigParam, ConfigResult> configStrategy;

    // @Scheduled(cron = "0/15 * * * * *")
    public void execute() {
        EmailParam emailParam = new EmailParam();
        emailParam.setSchedulerConfiguration(schedulerConfiguration);
        emailParam
                .setSchedulerControllerStatistics(schedulerControllerStatistics);
        emailParam.setConfigStrategy(configStrategy);
        schedulerConfiguration.getEmailUtil().receiveConfigEmail(emailParam);
    }
}
