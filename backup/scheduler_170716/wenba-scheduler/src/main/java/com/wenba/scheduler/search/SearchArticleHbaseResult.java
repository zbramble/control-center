package com.wenba.scheduler.search;

/**
 * @author zhangbo
 *
 */
public class SearchArticleHbaseResult extends SearchResult {

    // 成员变量
    /**
     * user ID
     */
    private String uid;

    /**
     * keywords
     */
    private String keywords;

    /**
     * search article server for excute search
     */
    private SearchServer searchArticleServer;

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public SearchServer getSearchArticleServer() {
        return searchArticleServer;
    }

    public void setSearchArticleServer(SearchServer searchArticleServer) {
        this.searchArticleServer = searchArticleServer;
    }

}
