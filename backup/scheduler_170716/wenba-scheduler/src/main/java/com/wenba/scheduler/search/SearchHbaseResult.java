package com.wenba.scheduler.search;

/**
 * @author zhangbo
 *
 */
public class SearchHbaseResult extends SearchResult {

    // 成员变量
    /**
     * feed ID
     */
    private long fid;

    /**
     * user ID
     */
    private int uid;

    /**
     * search server for excute search
     */
    private SearchServer searchServer;

    public long getFid() {
        return fid;
    }

    public void setFid(long fid) {
        this.fid = fid;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public SearchServer getSearchServer() {
        return searchServer;
    }

    public void setSearchServer(SearchServer searchServer) {
        this.searchServer = searchServer;
    }

}
