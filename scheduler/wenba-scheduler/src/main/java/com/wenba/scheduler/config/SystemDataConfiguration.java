package com.wenba.scheduler.config;

/**
 * @author zhangbo
 *
 */
public class SystemDataConfiguration {

    // 成员变量
    /**
     * Scheduler ID
     */
    private String schedulerId;

    /**
     * cnn server weight
     */
    private int ocrServerCnnWeight;

    /**
     * java server weight
     */
    private int ocrServerJavaWeight;

    /**
     * ocr server total weight
     */
    private int ocrServerTotalWeight;

    /**
     * search similarity threshold(maybe need 2nd ocr)
     */
    private float thresUnusedResult;

    /**
     * search result limit num
     */
    private int limit;

    /**
     * marks threshold
     */
    private int marksThreshold;

    /**
     * scheduler strategy
     */
    private SchedulerStrategy schedulerStrategy;

    /**
     * cnn process number threshold
     */
    private int cnnProcessNumThres;

    /**
     * cnn process max number threshold
     */
    private int cnnProcessMaxNumThres;

    /**
     * max cnn process number
     */
    private int maxCnnProcessNum;

    /**
     * search process number threshold
     */
    private int searchProcessNumThres;

    /**
     * max search process number
     */
    private int maxSearchProcessNum;

    /**
     * search query url
     */
    private String searchQuery;

    /**
     * search query by id url
     */
    private String searchQueryById;

    /**
     * search query by id user
     */
    private String searchQueryByIdUser;

    /**
     * search query by id token
     */
    private String searchQueryByIdToken;

    /**
     * word search query url
     */
    private String wordSearchQuery;

    /**
     * classic poem query url
     */
    private String classicPoemQuery;

    /**
     * classic poem auto complete url
     */
    private String classicPoemAutoComplete;

    /**
     * article query url
     */
    private String articleQuery;

    /**
     * article query by id url
     */
    private String articleQueryById;

    /**
     * article auto complete url
     */
    private String articleAutoComplete;

    /**
     * article query log address
     */
    private String articleQueryLogAddress;

    /**
     * article auto complete log address
     */
    private String articleAutoCompleteLogAddress;

    /**
     * em query url
     */
    private String emQuery;

    /**
     * async queue size
     */
    private int asyncQueueSize;

    /**
     * query request limit
     */
    private int queryRequestLimit;

    /**
     * query BI queue limit
     */
    private int queryBiQueueLimit;

    /**
     * jedis ip
     */
    private String jedisIp;

    /**
     * jedis port
     */
    private int jedisPort;

    /**
     * local jedis ip
     */
    private String localJedisIp;

    /**
     * local jedis port
     */
    private int localJedisPort;

    /**
     * redis expire seconds
     */
    private int redisExpireSeconds;

    /**
     * max total connections
     */
    private int maxTotalConnections;

    /**
     * default max connections per route
     */
    private int defaultMaxConnectionsPerRoute;

    /**
     * cnn connections per route
     */
    private int cnnConnectionsPerRoute;

    /**
     * search connections per route
     */
    private int searchConnectionsPerRoute;

    /**
     * ugc handwrite ocr connections
     */
    // private int ugcHandwriteOcrConnections;

    public String getSchedulerId() {
        return schedulerId;
    }

    public void setSchedulerId(String schedulerId) {
        this.schedulerId = schedulerId;
    }

    public int getOcrServerCnnWeight() {
        return ocrServerCnnWeight;
    }

    public void setOcrServerCnnWeight(int ocrServerCnnWeight) {
        this.ocrServerCnnWeight = ocrServerCnnWeight;
    }

    public int getOcrServerJavaWeight() {
        return ocrServerJavaWeight;
    }

    public void setOcrServerJavaWeight(int ocrServerJavaWeight) {
        this.ocrServerJavaWeight = ocrServerJavaWeight;
    }

    public int getOcrServerTotalWeight() {
        return ocrServerTotalWeight;
    }

    public void setOcrServerTotalWeight(int ocrServerTotalWeight) {
        this.ocrServerTotalWeight = ocrServerTotalWeight;
    }

    public float getThresUnusedResult() {
        return thresUnusedResult;
    }

    public void setThresUnusedResult(float thresUnusedResult) {
        this.thresUnusedResult = thresUnusedResult;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public int getMarksThreshold() {
        return marksThreshold;
    }

    public void setMarksThreshold(int marksThreshold) {
        this.marksThreshold = marksThreshold;
    }

    public SchedulerStrategy getSchedulerStrategy() {
        return schedulerStrategy;
    }

    public void setSchedulerStrategy(SchedulerStrategy schedulerStrategy) {
        this.schedulerStrategy = schedulerStrategy;
    }

    public int getCnnProcessNumThres() {
        return cnnProcessNumThres;
    }

    public void setCnnProcessNumThres(int cnnProcessNumThres) {
        this.cnnProcessNumThres = cnnProcessNumThres;
    }

    public int getCnnProcessMaxNumThres() {
        return cnnProcessMaxNumThres;
    }

    public void setCnnProcessMaxNumThres(int cnnProcessMaxNumThres) {
        this.cnnProcessMaxNumThres = cnnProcessMaxNumThres;
    }

    public int getMaxCnnProcessNum() {
        return maxCnnProcessNum;
    }

    public void setMaxCnnProcessNum(int maxCnnProcessNum) {
        this.maxCnnProcessNum = maxCnnProcessNum;
    }

    public int getSearchProcessNumThres() {
        return searchProcessNumThres;
    }

    public void setSearchProcessNumThres(int searchProcessNumThres) {
        this.searchProcessNumThres = searchProcessNumThres;
    }

    public int getMaxSearchProcessNum() {
        return maxSearchProcessNum;
    }

    public void setMaxSearchProcessNum(int maxSearchProcessNum) {
        this.maxSearchProcessNum = maxSearchProcessNum;
    }

    public String getSearchQuery() {
        return searchQuery;
    }

    public void setSearchQuery(String searchQuery) {
        this.searchQuery = searchQuery;
    }

    public String getSearchQueryById() {
        return searchQueryById;
    }

    public void setSearchQueryById(String searchQueryById) {
        this.searchQueryById = searchQueryById;
    }

    public String getSearchQueryByIdUser() {
        return searchQueryByIdUser;
    }

    public void setSearchQueryByIdUser(String searchQueryByIdUser) {
        this.searchQueryByIdUser = searchQueryByIdUser;
    }

    public String getSearchQueryByIdToken() {
        return searchQueryByIdToken;
    }

    public void setSearchQueryByIdToken(String searchQueryByIdToken) {
        this.searchQueryByIdToken = searchQueryByIdToken;
    }

    public String getWordSearchQuery() {
        return wordSearchQuery;
    }

    public void setWordSearchQuery(String wordSearchQuery) {
        this.wordSearchQuery = wordSearchQuery;
    }

    public String getClassicPoemQuery() {
        return classicPoemQuery;
    }

    public void setClassicPoemQuery(String classicPoemQuery) {
        this.classicPoemQuery = classicPoemQuery;
    }

    public String getClassicPoemAutoComplete() {
        return classicPoemAutoComplete;
    }

    public void setClassicPoemAutoComplete(String classicPoemAutoComplete) {
        this.classicPoemAutoComplete = classicPoemAutoComplete;
    }

    public String getArticleQuery() {
        return articleQuery;
    }

    public void setArticleQuery(String articleQuery) {
        this.articleQuery = articleQuery;
    }

    public String getArticleQueryById() {
        return articleQueryById;
    }

    public void setArticleQueryById(String articleQueryById) {
        this.articleQueryById = articleQueryById;
    }

    public String getArticleAutoComplete() {
        return articleAutoComplete;
    }

    public void setArticleAutoComplete(String articleAutoComplete) {
        this.articleAutoComplete = articleAutoComplete;
    }

    public String getArticleQueryLogAddress() {
        return articleQueryLogAddress;
    }

    public void setArticleQueryLogAddress(String articleQueryLogAddress) {
        this.articleQueryLogAddress = articleQueryLogAddress;
    }

    public String getArticleAutoCompleteLogAddress() {
        return articleAutoCompleteLogAddress;
    }

    public void setArticleAutoCompleteLogAddress(
            String articleAutoCompleteLogAddress) {
        this.articleAutoCompleteLogAddress = articleAutoCompleteLogAddress;
    }

    public String getEmQuery() {
        return emQuery;
    }

    public void setEmQuery(String emQuery) {
        this.emQuery = emQuery;
    }

    public int getAsyncQueueSize() {
        return asyncQueueSize;
    }

    public void setAsyncQueueSize(int asyncQueueSize) {
        this.asyncQueueSize = asyncQueueSize;
    }

    public int getQueryRequestLimit() {
        return queryRequestLimit;
    }

    public void setQueryRequestLimit(int queryRequestLimit) {
        this.queryRequestLimit = queryRequestLimit;
    }

    public int getQueryBiQueueLimit() {
        return queryBiQueueLimit;
    }

    public void setQueryBiQueueLimit(int queryBiQueueLimit) {
        this.queryBiQueueLimit = queryBiQueueLimit;
    }

    public String getJedisIp() {
        return jedisIp;
    }

    public void setJedisIp(String jedisIp) {
        this.jedisIp = jedisIp;
    }

    public int getJedisPort() {
        return jedisPort;
    }

    public void setJedisPort(int jedisPort) {
        this.jedisPort = jedisPort;
    }

    public String getLocalJedisIp() {
        return localJedisIp;
    }

    public void setLocalJedisIp(String localJedisIp) {
        this.localJedisIp = localJedisIp;
    }

    public int getLocalJedisPort() {
        return localJedisPort;
    }

    public void setLocalJedisPort(int localJedisPort) {
        this.localJedisPort = localJedisPort;
    }

    public int getRedisExpireSeconds() {
        return redisExpireSeconds;
    }

    public void setRedisExpireSeconds(int redisExpireSeconds) {
        this.redisExpireSeconds = redisExpireSeconds;
    }

    public int getMaxTotalConnections() {
        return maxTotalConnections;
    }

    public void setMaxTotalConnections(int maxTotalConnections) {
        this.maxTotalConnections = maxTotalConnections;
    }

    public int getDefaultMaxConnectionsPerRoute() {
        return defaultMaxConnectionsPerRoute;
    }

    public void setDefaultMaxConnectionsPerRoute(
            int defaultMaxConnectionsPerRoute) {
        this.defaultMaxConnectionsPerRoute = defaultMaxConnectionsPerRoute;
    }

    public int getCnnConnectionsPerRoute() {
        return cnnConnectionsPerRoute;
    }

    public void setCnnConnectionsPerRoute(int cnnConnectionsPerRoute) {
        this.cnnConnectionsPerRoute = cnnConnectionsPerRoute;
    }

    public int getSearchConnectionsPerRoute() {
        return searchConnectionsPerRoute;
    }

    public void setSearchConnectionsPerRoute(int searchConnectionsPerRoute) {
        this.searchConnectionsPerRoute = searchConnectionsPerRoute;
    }

    // public int getUgcHandwriteOcrConnections() {
    // return ugcHandwriteOcrConnections;
    // }
    //
    // public void setUgcHandwriteOcrConnections(int ugcHandwriteOcrConnections)
    // {
    // this.ugcHandwriteOcrConnections = ugcHandwriteOcrConnections;
    // }

    /**
     * @author zhangbo
     *
     */
    public enum SchedulerStrategy {
        RANDOM, POLLING, OTHER;
    }

}
