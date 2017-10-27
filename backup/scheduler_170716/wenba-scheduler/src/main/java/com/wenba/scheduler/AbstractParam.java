package com.wenba.scheduler;

import com.wenba.scheduler.config.SchedulerConfiguration;

/**
 * @author zhangbo
 *
 */
public class AbstractParam {

    // 成员变量
    /**
     * Scheduler Configuration
     */
    private SchedulerConfiguration schedulerConfiguration;

    /**
     * Scheduler Controller Statistics
     */
    private SchedulerControllerStatistics schedulerControllerStatistics;

    /**
     * feed ID
     */
    private String fid;

    /**
     * user ID
     */
    private String uid;

    /**
     * app
     */
    private String app;

    public SchedulerConfiguration getSchedulerConfiguration() {
        return schedulerConfiguration;
    }

    public void setSchedulerConfiguration(
            SchedulerConfiguration schedulerConfiguration) {
        this.schedulerConfiguration = schedulerConfiguration;
    }

    public SchedulerControllerStatistics getSchedulerControllerStatistics() {
        return schedulerControllerStatistics;
    }

    public void setSchedulerControllerStatistics(
            SchedulerControllerStatistics schedulerControllerStatistics) {
        this.schedulerControllerStatistics = schedulerControllerStatistics;
    }

    public String getFid() {
        return fid;
    }

    public void setFid(String fid) {
        this.fid = fid;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }

}
