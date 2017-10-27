package com.wenba.scheduler.config;

/**
 * @author zhangbo
 *
 */
public class TimeoutConfiguration {

    // 成员变量
    /**
     * connect timeout
     */
    private int connectTimeout;

    /**
     * ocr timeout
     */
    private int ocrTimeout;

    /**
     * search timeout
     */
    private int searchTimeout;

    /**
     * search by id timeout
     */
    private int searchByIdTimeout;

    /**
     * search homework timeout
     */
    private int searchHomeworkTimeout;

    /**
     * search article timeout
     */
    private int searchArticleTimeout;

    /**
     * nlp timeout
     */
    private int nlpTimeout;

    /**
     * bi timeout
     */
    private int biTimeout;

    /**
     * query timeout
     */
    private int queryTimeout;

    /**
     * em timeout
     */
    private int emTimeout;

    /**
     * hbase timeout
     */
    private int hbaseTimeout;

    /**
     * hbase monitor time
     */
    private int hbaseMonitorTime;

    /**
     * ie timeout
     */
    private int ieTimeout;

    /**
     * ugc common timeout
     */
    private int ugcCommonTimeout;

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public int getOcrTimeout() {
        return ocrTimeout;
    }

    public void setOcrTimeout(int ocrTimeout) {
        this.ocrTimeout = ocrTimeout;
    }

    public int getSearchTimeout() {
        return searchTimeout;
    }

    public void setSearchTimeout(int searchTimeout) {
        this.searchTimeout = searchTimeout;
    }

    public int getSearchByIdTimeout() {
        return searchByIdTimeout;
    }

    public void setSearchByIdTimeout(int searchByIdTimeout) {
        this.searchByIdTimeout = searchByIdTimeout;
    }

    public int getSearchHomeworkTimeout() {
        return searchHomeworkTimeout;
    }

    public void setSearchHomeworkTimeout(int searchHomeworkTimeout) {
        this.searchHomeworkTimeout = searchHomeworkTimeout;
    }

    public int getSearchArticleTimeout() {
        return searchArticleTimeout;
    }

    public void setSearchArticleTimeout(int searchArticleTimeout) {
        this.searchArticleTimeout = searchArticleTimeout;
    }

    public int getNlpTimeout() {
        return nlpTimeout;
    }

    public void setNlpTimeout(int nlpTimeout) {
        this.nlpTimeout = nlpTimeout;
    }

    public int getBiTimeout() {
        return biTimeout;
    }

    public void setBiTimeout(int biTimeout) {
        this.biTimeout = biTimeout;
    }

    public int getQueryTimeout() {
        return queryTimeout;
    }

    public void setQueryTimeout(int queryTimeout) {
        this.queryTimeout = queryTimeout;
    }

    public int getEmTimeout() {
        return emTimeout;
    }

    public void setEmTimeout(int emTimeout) {
        this.emTimeout = emTimeout;
    }

    public int getHbaseTimeout() {
        return hbaseTimeout;
    }

    public void setHbaseTimeout(int hbaseTimeout) {
        this.hbaseTimeout = hbaseTimeout;
    }

    public int getHbaseMonitorTime() {
        return hbaseMonitorTime;
    }

    public void setHbaseMonitorTime(int hbaseMonitorTime) {
        this.hbaseMonitorTime = hbaseMonitorTime;
    }

    public int getIeTimeout() {
        return ieTimeout;
    }

    public void setIeTimeout(int ieTimeout) {
        this.ieTimeout = ieTimeout;
    }

    public int getUgcCommonTimeout() {
        return ugcCommonTimeout;
    }

    public void setUgcCommonTimeout(int ugcCommonTimeout) {
        this.ugcCommonTimeout = ugcCommonTimeout;
    }

}
