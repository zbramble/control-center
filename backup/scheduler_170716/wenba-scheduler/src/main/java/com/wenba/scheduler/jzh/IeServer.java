package com.wenba.scheduler.jzh;

import com.wenba.scheduler.AbstractServer;

/**
 * @author zhangbo
 *
 */
public class IeServer extends AbstractServer {

    // 成员变量
    private IeServerStatistics ieServerStatistics;

    public IeServerStatistics getIeServerStatistics() {
        return ieServerStatistics;
    }

    public void setIeServerStatistics(IeServerStatistics ieServerStatistics) {
        this.ieServerStatistics = ieServerStatistics;
    }

}
