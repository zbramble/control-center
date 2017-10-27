package com.wenba.scheduler.search;

import java.util.List;

import com.wenba.scheduler.AbstractResult;

/**
 * @author zhangbo
 *
 */
public class SearchResult extends AbstractResult {

    // 成员变量
    /**
     * max similarity
     */
    private float maxSimilarity;

    /**
     * answer
     */
    private String answer;

    /**
     * search result list(SearchResult:id,similarity)
     */
    private List<com.xueba100.mining.common.SearchResult> searchResultList;

    public float getMaxSimilarity() {
        return maxSimilarity;
    }

    public void setMaxSimilarity(float maxSimilarity) {
        this.maxSimilarity = maxSimilarity;
    }

    public List<com.xueba100.mining.common.SearchResult> getSearchResultList() {
        return searchResultList;
    }

    public void setSearchResultList(
            List<com.xueba100.mining.common.SearchResult> searchResultList) {
        this.searchResultList = searchResultList;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

}
