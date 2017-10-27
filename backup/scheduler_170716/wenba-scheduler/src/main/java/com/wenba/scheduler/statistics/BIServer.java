package com.wenba.scheduler.statistics;

import com.wenba.scheduler.AbstractServer;

/**
 * @author zhangbo
 *
 */
public class BIServer extends AbstractServer {

    // 成员变量
    private BIServerStatistics biServerStatistics;

    public BIServerStatistics getBiServerStatistics() {
        return biServerStatistics;
    }

    public void setBiServerStatistics(BIServerStatistics biServerStatistics) {
        this.biServerStatistics = biServerStatistics;
    }

}
