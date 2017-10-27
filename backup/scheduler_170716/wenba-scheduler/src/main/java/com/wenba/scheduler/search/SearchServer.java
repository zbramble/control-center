package com.wenba.scheduler.search;

import com.wenba.scheduler.AbstractServer;

/**
 * @author zhangbo
 *
 */
public class SearchServer extends AbstractServer {

    // 成员变量
    private SearchServerStatistics searchServerStatistics;

    public SearchServerStatistics getSearchServerStatistics() {
        return searchServerStatistics;
    }

    public void setSearchServerStatistics(
            SearchServerStatistics searchServerStatistics) {
        this.searchServerStatistics = searchServerStatistics;
    }

}
