package com.wenba.scheduler.em;

import com.wenba.scheduler.AbstractServer;

/**
 * @author zhangbo
 *
 */
public class EmServer extends AbstractServer {

    // 成员变量
    private EmServerStatistics emServerStatistics;

    public EmServerStatistics getEmServerStatistics() {
        return emServerStatistics;
    }

    public void setEmServerStatistics(EmServerStatistics emServerStatistics) {
        this.emServerStatistics = emServerStatistics;
    }

}
