package com.wenba.scheduler.search;

import com.wenba.scheduler.AbstractParam;
import com.wenba.scheduler.AbstractResult;

/**
 * @author zhangbo
 *
 */
public class SearchParam extends AbstractParam {

    // 成员变量
    /**
     * ocr result
     */
    private AbstractResult ocrResult;

    /**
     * search server for excute search
     */
    private SearchServer searchServer;

    /**
     * query by ids
     */
    private String ids;

    /**
     * uids for homewordk
     */
    private String uids;

    /**
     * search result limit num
     */
    private int limit;

    /**
     * keywords
     */
    private String keywords;

    /**
     * filter
     */
    private String filter;

    /**
     * grade
     */
    private String grade;

    /**
     * tags limit
     */
    private Integer tagsLimit;

    /**
     * page no
     */
    private Integer pageNo;

    /**
     * page size
     */
    private Integer pageSize;

    /**
     * article url
     */
    private String articleUrl;

    /**
     * article type
     */
    private ArticleType articleType;

    /**
     * classic poem url
     */
    private String classicPoemUrl;

    /**
     * classic poem type
     */
    private ClassicPoemType classicPoemType;

    /**
     * subject
     */
    private String subject;

    /**
     * index
     */
    private String index;

    /**
     * user
     */
    private String user;

    /**
     * token
     */
    private String token;

    /**
     * book ids
     */
    private String bookIds;

    public AbstractResult getOcrResult() {
        return ocrResult;
    }

    public void setOcrResult(AbstractResult ocrResult) {
        this.ocrResult = ocrResult;
    }

    public SearchServer getSearchServer() {
        return searchServer;
    }

    public void setSearchServer(SearchServer searchServer) {
        this.searchServer = searchServer;
    }

    public String getIds() {
        return ids;
    }

    public void setIds(String ids) {
        this.ids = ids;
    }

    public String getUids() {
        return uids;
    }

    public void setUids(String uids) {
        this.uids = uids;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public Integer getTagsLimit() {
        return tagsLimit;
    }

    public void setTagsLimit(Integer tagsLimit) {
        this.tagsLimit = tagsLimit;
    }

    public Integer getPageNo() {
        return pageNo;
    }

    public void setPageNo(Integer pageNo) {
        this.pageNo = pageNo;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public String getArticleUrl() {
        return articleUrl;
    }

    public void setArticleUrl(String articleUrl) {
        this.articleUrl = articleUrl;
    }

    public ArticleType getArticleType() {
        return articleType;
    }

    public void setArticleType(ArticleType articleType) {
        this.articleType = articleType;
    }

    public String getClassicPoemUrl() {
        return classicPoemUrl;
    }

    public void setClassicPoemUrl(String classicPoemUrl) {
        this.classicPoemUrl = classicPoemUrl;
    }

    public ClassicPoemType getClassicPoemType() {
        return classicPoemType;
    }

    public void setClassicPoemType(ClassicPoemType classicPoemType) {
        this.classicPoemType = classicPoemType;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getBookIds() {
        return bookIds;
    }

    public void setBookIds(String bookIds) {
        this.bookIds = bookIds;
    }

    /**
     * @author zhangbo
     *
     */
    public enum ArticleType {
        QUERY, QUERY_BY_ID, AUTO_COMPLETE;
    }

    /**
     * @author zhangbo
     *
     */
    public enum ClassicPoemType {
        QUERY, AUTO_COMPLETE;
    }

}
