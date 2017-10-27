package com.wenba.scheduler.config;

import com.wenba.scheduler.AbstractServer;

/**
 * @author zhangbo
 *
 */
public class ConfigServer extends AbstractServer {

    // 成员变量
    private ConfigServerStatistics configServerStatistics;

    public ConfigServerStatistics getConfigServerStatistics() {
        return configServerStatistics;
    }

    public void setConfigServerStatistics(
            ConfigServerStatistics configServerStatistics) {
        this.configServerStatistics = configServerStatistics;
    }

}
