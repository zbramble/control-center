package com.wenba.scheduler.search;

/**
 * @author zhangbo
 *
 */
public class SearchArticleLog extends SearchParam {

    // 成员变量
    /**
     * query time
     */
    private Long queryTime;

    /**
     * last keywords
     */
    private String lastKeywords;

    /**
     * last query time
     */
    private Long lastQueryTime;

    /**
     * session id
     */
    private String sessionId;

    /**
     * search result
     */
    private SearchResult searchResult;

    /**
     * server index
     */
    private int serverIndex;

    public Long getQueryTime() {
        return queryTime;
    }

    public void setQueryTime(Long queryTime) {
        this.queryTime = queryTime;
    }

    public String getLastKeywords() {
        return lastKeywords;
    }

    public void setLastKeywords(String lastKeywords) {
        this.lastKeywords = lastKeywords;
    }

    public Long getLastQueryTime() {
        return lastQueryTime;
    }

    public void setLastQueryTime(Long lastQueryTime) {
        this.lastQueryTime = lastQueryTime;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public SearchResult getSearchResult() {
        return searchResult;
    }

    public void setSearchResult(SearchResult searchResult) {
        this.searchResult = searchResult;
    }

    public int getServerIndex() {
        return serverIndex;
    }

    public void setServerIndex(int serverIndex) {
        this.serverIndex = serverIndex;
    }

}
