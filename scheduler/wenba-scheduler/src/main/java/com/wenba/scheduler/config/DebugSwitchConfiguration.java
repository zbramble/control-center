package com.wenba.scheduler.config;

/**
 * @author zhangbo
 *
 */
public class DebugSwitchConfiguration {

    // 成员变量
    /**
     * 1.all request, current request, hbase queue num debug switch
     */
    private boolean allRequestAndHbaseQueueDebugSwitch;

    /**
     * 2.current request debug switch
     */
    private boolean currentRequestDebugSwitch;

    /**
     * 3.httpclient debug switch
     */
    private boolean httpclientDebugSwitch;

    /**
     * 4.GC debug switch
     */
    private boolean gcDebugSwitch;

    /**
     * 5.display marks debug switch
     */
    private boolean displayMarksDebugSwitch;

    /**
     * 6.current cnn ocr request debugswitch
     */
    private boolean currentCnnOcrRequestDebugSwitch;

    /**
     * 7.current search request debugswitch
     */
    private boolean currentSearchRequestDebugSwitch;

    /**
     * 8.current nlp request debugswitch
     */
    private boolean currentNlpRequestDebugSwitch;

    /**
     * 9.avg query exec time debug switch
     */
    private boolean avgQueryExecTimeDebugSwitch;

    /**
     * 10.avg query by id exec time debug switch
     */
    private boolean avgQueryByIdExecTimeDebugSwitch;

    /**
     * 11.avg mining homework exec time debug switch
     */
    private boolean avgHomeworkExecTimeDebugSwitch;

    /**
     * 12.avg search exec time debug switch
     */
    private boolean avgSearchExecTimeDebugSwitch;

    /**
     * 13.avg word search exec time debug switch
     */
    private boolean avgWordSearchExecTimeDebugSwitch;

    /**
     * 14.avg search article exec time debug switch
     */
    private boolean avgArticleExecTimeDebugSwitch;

    /**
     * 15.avg save ocr hbase exec time debug switch
     */
    private boolean avgOcrHbaseExecTimeDebugSwitch;

    /**
     * 16.avg save search hbase exec time debug switch
     */
    private boolean avgSearchHbaseExecTimeDebugSwitch;

    /**
     * 17.avg save search article hbase exec time debug switch
     */
    private boolean avgSearchArticleHbaseExecTimeDebugSwitch;

    /**
     * 18.avg cnn server exec time debug switch
     */
    private boolean avgCnnServerExecTimeDebugSwitch;

    /**
     * 19.avg java server exec time debug switch
     */
    private boolean avgJavaServerExecTimeDebugSwitch;

    /**
     * 20.avg search server exec time debug switch
     */
    private boolean avgSearchServerExecTimeDebugSwitch;

    /**
     * avg word search server exec time debug switch
     */
    private boolean avgWordSearchServerExecTimeDebugSwitch;

    /**
     * 21.avg search by id server exec time debug switch
     */
    private boolean avgSearchByIdServerExecTimeDebugSwitch;

    /**
     * 22.avg search homework server exec time debug switch
     */
    private boolean avgSearchHomeworkServerExecTimeDebugSwitch;

    /**
     * 23.avg search article server exec time debug switch
     */
    private boolean avgSearchArticleServerExecTimeDebugSwitch;

    /**
     * 24.avg nlp server exec time debug switch
     */
    private boolean avgNlpServerExecTimeDebugSwitch;

    /**
     * 25.query exec time debug switch
     */
    private boolean queryExecTimeDebugSwitch;

    /**
     * 26.query by id exec time debug switch
     */
    private boolean queryByIdExecTimeDebugSwitch;

    /**
     * 27.mining homework exec time debug switch
     */
    private boolean homeworkExecTimeDebugSwitch;

    /**
     * 28.search exec time debug switch
     */
    private boolean searchExecTimeDebugSwitch;

    /**
     * 29.word search exec time debug switch
     */
    private boolean wordSearchExecTimeDebugSwitch;

    /**
     * 30.search article exec time debug switch
     */
    private boolean articleExecTimeDebugSwitch;

    /**
     * classic poem query exec time debug switch
     */
    private boolean classicPoemQueryExecTimeDebugSwitch;

    /**
     * classic poem auto complete exec time debug switch
     */
    private boolean classicPoemAutoCompleteExecTimeDebugSwitch;

    /**
     * em query exec time debug switch
     */
    private boolean emQueryExecTimeDebugSwitch;

    /**
     * 31.save ocr to hbase exec time
     */
    private boolean ocrHbaseExecTimeDebugSwitch;

    /**
     * 32.save search to hbase exec time
     */
    private boolean searchHbaseExecTimeDebugSwitch;

    /**
     * 33.save search article to hbase exec time
     */
    private boolean searchArticleHbaseExecTimeDebugSwitch;

    /**
     * 34.cnn server exec time debug switch
     */
    private boolean cnnServerExecTimeDebugSwitch;

    /**
     * 35.java server exec time debug switch
     */
    private boolean javaServerExecTimeDebugSwitch;

    /**
     * 36.search server exec time debug switch
     */
    private boolean searchServerExecTimeDebugSwitch;

    /**
     * word search server exec time debug switch
     */
    private boolean wordSearchServerExecTimeDebugSwitch;

    /**
     * 37.search by id server exec time debug switch
     */
    private boolean searchByIdServerExecTimeDebugSwitch;

    /**
     * 38.search homework server exec time debug switch
     */
    private boolean searchHomeworkServerExecTimeDebugSwitch;

    /**
     * 39.search article server exec time debug switch
     */
    private boolean searchArticleServerExecTimeDebugSwitch;

    /**
     * 40.nlp server exec time debug switch
     */
    private boolean nlpServerExecTimeDebugSwitch;

    /**
     * 41.redis debug switch
     */
    private boolean redisDebugSwitch;

    /**
     * backup hbase debug switch
     */
    private boolean bkHbaseDebugSwitch;

    public boolean isSearchExecTimeDebugSwitch() {
        return searchExecTimeDebugSwitch;
    }

    public void setSearchExecTimeDebugSwitch(boolean searchExecTimeDebugSwitch) {
        this.searchExecTimeDebugSwitch = searchExecTimeDebugSwitch;
    }

    public boolean isWordSearchExecTimeDebugSwitch() {
        return wordSearchExecTimeDebugSwitch;
    }

    public void setWordSearchExecTimeDebugSwitch(
            boolean wordSearchExecTimeDebugSwitch) {
        this.wordSearchExecTimeDebugSwitch = wordSearchExecTimeDebugSwitch;
    }

    public boolean isAllRequestAndHbaseQueueDebugSwitch() {
        return allRequestAndHbaseQueueDebugSwitch;
    }

    public void setAllRequestAndHbaseQueueDebugSwitch(
            boolean allRequestAndHbaseQueueDebugSwitch) {
        this.allRequestAndHbaseQueueDebugSwitch = allRequestAndHbaseQueueDebugSwitch;
    }

    public boolean isAvgQueryExecTimeDebugSwitch() {
        return avgQueryExecTimeDebugSwitch;
    }

    public void setAvgQueryExecTimeDebugSwitch(
            boolean avgQueryExecTimeDebugSwitch) {
        this.avgQueryExecTimeDebugSwitch = avgQueryExecTimeDebugSwitch;
    }

    public boolean isAvgQueryByIdExecTimeDebugSwitch() {
        return avgQueryByIdExecTimeDebugSwitch;
    }

    public void setAvgQueryByIdExecTimeDebugSwitch(
            boolean avgQueryByIdExecTimeDebugSwitch) {
        this.avgQueryByIdExecTimeDebugSwitch = avgQueryByIdExecTimeDebugSwitch;
    }

    public boolean isAvgHomeworkExecTimeDebugSwitch() {
        return avgHomeworkExecTimeDebugSwitch;
    }

    public void setAvgHomeworkExecTimeDebugSwitch(
            boolean avgHomeworkExecTimeDebugSwitch) {
        this.avgHomeworkExecTimeDebugSwitch = avgHomeworkExecTimeDebugSwitch;
    }

    public boolean isAvgSearchExecTimeDebugSwitch() {
        return avgSearchExecTimeDebugSwitch;
    }

    public void setAvgSearchExecTimeDebugSwitch(
            boolean avgSearchExecTimeDebugSwitch) {
        this.avgSearchExecTimeDebugSwitch = avgSearchExecTimeDebugSwitch;
    }

    public boolean isAvgWordSearchExecTimeDebugSwitch() {
        return avgWordSearchExecTimeDebugSwitch;
    }

    public void setAvgWordSearchExecTimeDebugSwitch(
            boolean avgWordSearchExecTimeDebugSwitch) {
        this.avgWordSearchExecTimeDebugSwitch = avgWordSearchExecTimeDebugSwitch;
    }

    public boolean isAvgArticleExecTimeDebugSwitch() {
        return avgArticleExecTimeDebugSwitch;
    }

    public void setAvgArticleExecTimeDebugSwitch(
            boolean avgArticleExecTimeDebugSwitch) {
        this.avgArticleExecTimeDebugSwitch = avgArticleExecTimeDebugSwitch;
    }

    public boolean isAvgOcrHbaseExecTimeDebugSwitch() {
        return avgOcrHbaseExecTimeDebugSwitch;
    }

    public void setAvgOcrHbaseExecTimeDebugSwitch(
            boolean avgOcrHbaseExecTimeDebugSwitch) {
        this.avgOcrHbaseExecTimeDebugSwitch = avgOcrHbaseExecTimeDebugSwitch;
    }

    public boolean isAvgSearchHbaseExecTimeDebugSwitch() {
        return avgSearchHbaseExecTimeDebugSwitch;
    }

    public void setAvgSearchHbaseExecTimeDebugSwitch(
            boolean avgSearchHbaseExecTimeDebugSwitch) {
        this.avgSearchHbaseExecTimeDebugSwitch = avgSearchHbaseExecTimeDebugSwitch;
    }

    public boolean isAvgCnnServerExecTimeDebugSwitch() {
        return avgCnnServerExecTimeDebugSwitch;
    }

    public void setAvgCnnServerExecTimeDebugSwitch(
            boolean avgCnnServerExecTimeDebugSwitch) {
        this.avgCnnServerExecTimeDebugSwitch = avgCnnServerExecTimeDebugSwitch;
    }

    public boolean isAvgJavaServerExecTimeDebugSwitch() {
        return avgJavaServerExecTimeDebugSwitch;
    }

    public void setAvgJavaServerExecTimeDebugSwitch(
            boolean avgJavaServerExecTimeDebugSwitch) {
        this.avgJavaServerExecTimeDebugSwitch = avgJavaServerExecTimeDebugSwitch;
    }

    public boolean isAvgSearchServerExecTimeDebugSwitch() {
        return avgSearchServerExecTimeDebugSwitch;
    }

    public void setAvgSearchServerExecTimeDebugSwitch(
            boolean avgSearchServerExecTimeDebugSwitch) {
        this.avgSearchServerExecTimeDebugSwitch = avgSearchServerExecTimeDebugSwitch;
    }

    public boolean isAvgSearchByIdServerExecTimeDebugSwitch() {
        return avgSearchByIdServerExecTimeDebugSwitch;
    }

    public void setAvgSearchByIdServerExecTimeDebugSwitch(
            boolean avgSearchByIdServerExecTimeDebugSwitch) {
        this.avgSearchByIdServerExecTimeDebugSwitch = avgSearchByIdServerExecTimeDebugSwitch;
    }

    public boolean isAvgSearchHomeworkServerExecTimeDebugSwitch() {
        return avgSearchHomeworkServerExecTimeDebugSwitch;
    }

    public void setAvgSearchHomeworkServerExecTimeDebugSwitch(
            boolean avgSearchHomeworkServerExecTimeDebugSwitch) {
        this.avgSearchHomeworkServerExecTimeDebugSwitch = avgSearchHomeworkServerExecTimeDebugSwitch;
    }

    public boolean isAvgSearchArticleServerExecTimeDebugSwitch() {
        return avgSearchArticleServerExecTimeDebugSwitch;
    }

    public void setAvgSearchArticleServerExecTimeDebugSwitch(
            boolean avgSearchArticleServerExecTimeDebugSwitch) {
        this.avgSearchArticleServerExecTimeDebugSwitch = avgSearchArticleServerExecTimeDebugSwitch;
    }

    public boolean isAvgNlpServerExecTimeDebugSwitch() {
        return avgNlpServerExecTimeDebugSwitch;
    }

    public void setAvgNlpServerExecTimeDebugSwitch(
            boolean avgNlpServerExecTimeDebugSwitch) {
        this.avgNlpServerExecTimeDebugSwitch = avgNlpServerExecTimeDebugSwitch;
    }

    public boolean isCurrentRequestDebugSwitch() {
        return currentRequestDebugSwitch;
    }

    public void setCurrentRequestDebugSwitch(boolean currentRequestDebugSwitch) {
        this.currentRequestDebugSwitch = currentRequestDebugSwitch;
    }

    public boolean isHttpclientDebugSwitch() {
        return httpclientDebugSwitch;
    }

    public void setHttpclientDebugSwitch(boolean httpclientDebugSwitch) {
        this.httpclientDebugSwitch = httpclientDebugSwitch;
    }

    public boolean isOcrHbaseExecTimeDebugSwitch() {
        return ocrHbaseExecTimeDebugSwitch;
    }

    public void setOcrHbaseExecTimeDebugSwitch(
            boolean ocrHbaseExecTimeDebugSwitch) {
        this.ocrHbaseExecTimeDebugSwitch = ocrHbaseExecTimeDebugSwitch;
    }

    public boolean isSearchHbaseExecTimeDebugSwitch() {
        return searchHbaseExecTimeDebugSwitch;
    }

    public void setSearchHbaseExecTimeDebugSwitch(
            boolean searchHbaseExecTimeDebugSwitch) {
        this.searchHbaseExecTimeDebugSwitch = searchHbaseExecTimeDebugSwitch;
    }

    public boolean isGcDebugSwitch() {
        return gcDebugSwitch;
    }

    public void setGcDebugSwitch(boolean gcDebugSwitch) {
        this.gcDebugSwitch = gcDebugSwitch;
    }

    public boolean isDisplayMarksDebugSwitch() {
        return displayMarksDebugSwitch;
    }

    public void setDisplayMarksDebugSwitch(boolean displayMarksDebugSwitch) {
        this.displayMarksDebugSwitch = displayMarksDebugSwitch;
    }

    public boolean isCurrentCnnOcrRequestDebugSwitch() {
        return currentCnnOcrRequestDebugSwitch;
    }

    public void setCurrentCnnOcrRequestDebugSwitch(
            boolean currentCnnOcrRequestDebugSwitch) {
        this.currentCnnOcrRequestDebugSwitch = currentCnnOcrRequestDebugSwitch;
    }

    public boolean isCurrentSearchRequestDebugSwitch() {
        return currentSearchRequestDebugSwitch;
    }

    public void setCurrentSearchRequestDebugSwitch(
            boolean currentSearchRequestDebugSwitch) {
        this.currentSearchRequestDebugSwitch = currentSearchRequestDebugSwitch;
    }

    public boolean isCurrentNlpRequestDebugSwitch() {
        return currentNlpRequestDebugSwitch;
    }

    public void setCurrentNlpRequestDebugSwitch(
            boolean currentNlpRequestDebugSwitch) {
        this.currentNlpRequestDebugSwitch = currentNlpRequestDebugSwitch;
    }

    public boolean isQueryExecTimeDebugSwitch() {
        return queryExecTimeDebugSwitch;
    }

    public void setQueryExecTimeDebugSwitch(boolean queryExecTimeDebugSwitch) {
        this.queryExecTimeDebugSwitch = queryExecTimeDebugSwitch;
    }

    public boolean isQueryByIdExecTimeDebugSwitch() {
        return queryByIdExecTimeDebugSwitch;
    }

    public void setQueryByIdExecTimeDebugSwitch(
            boolean queryByIdExecTimeDebugSwitch) {
        this.queryByIdExecTimeDebugSwitch = queryByIdExecTimeDebugSwitch;
    }

    public boolean isHomeworkExecTimeDebugSwitch() {
        return homeworkExecTimeDebugSwitch;
    }

    public void setHomeworkExecTimeDebugSwitch(
            boolean homeworkExecTimeDebugSwitch) {
        this.homeworkExecTimeDebugSwitch = homeworkExecTimeDebugSwitch;
    }

    public boolean isArticleExecTimeDebugSwitch() {
        return articleExecTimeDebugSwitch;
    }

    public void setArticleExecTimeDebugSwitch(boolean articleExecTimeDebugSwitch) {
        this.articleExecTimeDebugSwitch = articleExecTimeDebugSwitch;
    }

    public boolean isClassicPoemQueryExecTimeDebugSwitch() {
        return classicPoemQueryExecTimeDebugSwitch;
    }

    public void setClassicPoemQueryExecTimeDebugSwitch(
            boolean classicPoemQueryExecTimeDebugSwitch) {
        this.classicPoemQueryExecTimeDebugSwitch = classicPoemQueryExecTimeDebugSwitch;
    }

    public boolean isClassicPoemAutoCompleteExecTimeDebugSwitch() {
        return classicPoemAutoCompleteExecTimeDebugSwitch;
    }

    public void setClassicPoemAutoCompleteExecTimeDebugSwitch(
            boolean classicPoemAutoCompleteExecTimeDebugSwitch) {
        this.classicPoemAutoCompleteExecTimeDebugSwitch = classicPoemAutoCompleteExecTimeDebugSwitch;
    }

    public boolean isEmQueryExecTimeDebugSwitch() {
        return emQueryExecTimeDebugSwitch;
    }

    public void setEmQueryExecTimeDebugSwitch(boolean emQueryExecTimeDebugSwitch) {
        this.emQueryExecTimeDebugSwitch = emQueryExecTimeDebugSwitch;
    }

    public boolean isCnnServerExecTimeDebugSwitch() {
        return cnnServerExecTimeDebugSwitch;
    }

    public void setCnnServerExecTimeDebugSwitch(
            boolean cnnServerExecTimeDebugSwitch) {
        this.cnnServerExecTimeDebugSwitch = cnnServerExecTimeDebugSwitch;
    }

    public boolean isJavaServerExecTimeDebugSwitch() {
        return javaServerExecTimeDebugSwitch;
    }

    public void setJavaServerExecTimeDebugSwitch(
            boolean javaServerExecTimeDebugSwitch) {
        this.javaServerExecTimeDebugSwitch = javaServerExecTimeDebugSwitch;
    }

    public boolean isSearchServerExecTimeDebugSwitch() {
        return searchServerExecTimeDebugSwitch;
    }

    public void setSearchServerExecTimeDebugSwitch(
            boolean searchServerExecTimeDebugSwitch) {
        this.searchServerExecTimeDebugSwitch = searchServerExecTimeDebugSwitch;
    }

    public boolean isSearchByIdServerExecTimeDebugSwitch() {
        return searchByIdServerExecTimeDebugSwitch;
    }

    public void setSearchByIdServerExecTimeDebugSwitch(
            boolean searchByIdServerExecTimeDebugSwitch) {
        this.searchByIdServerExecTimeDebugSwitch = searchByIdServerExecTimeDebugSwitch;
    }

    public boolean isSearchHomeworkServerExecTimeDebugSwitch() {
        return searchHomeworkServerExecTimeDebugSwitch;
    }

    public void setSearchHomeworkServerExecTimeDebugSwitch(
            boolean searchHomeworkServerExecTimeDebugSwitch) {
        this.searchHomeworkServerExecTimeDebugSwitch = searchHomeworkServerExecTimeDebugSwitch;
    }

    public boolean isSearchArticleServerExecTimeDebugSwitch() {
        return searchArticleServerExecTimeDebugSwitch;
    }

    public void setSearchArticleServerExecTimeDebugSwitch(
            boolean searchArticleServerExecTimeDebugSwitch) {
        this.searchArticleServerExecTimeDebugSwitch = searchArticleServerExecTimeDebugSwitch;
    }

    public boolean isNlpServerExecTimeDebugSwitch() {
        return nlpServerExecTimeDebugSwitch;
    }

    public void setNlpServerExecTimeDebugSwitch(
            boolean nlpServerExecTimeDebugSwitch) {
        this.nlpServerExecTimeDebugSwitch = nlpServerExecTimeDebugSwitch;
    }

    public boolean isAvgSearchArticleHbaseExecTimeDebugSwitch() {
        return avgSearchArticleHbaseExecTimeDebugSwitch;
    }

    public void setAvgSearchArticleHbaseExecTimeDebugSwitch(
            boolean avgSearchArticleHbaseExecTimeDebugSwitch) {
        this.avgSearchArticleHbaseExecTimeDebugSwitch = avgSearchArticleHbaseExecTimeDebugSwitch;
    }

    public boolean isSearchArticleHbaseExecTimeDebugSwitch() {
        return searchArticleHbaseExecTimeDebugSwitch;
    }

    public void setSearchArticleHbaseExecTimeDebugSwitch(
            boolean searchArticleHbaseExecTimeDebugSwitch) {
        this.searchArticleHbaseExecTimeDebugSwitch = searchArticleHbaseExecTimeDebugSwitch;
    }

    public boolean isAvgWordSearchServerExecTimeDebugSwitch() {
        return avgWordSearchServerExecTimeDebugSwitch;
    }

    public void setAvgWordSearchServerExecTimeDebugSwitch(
            boolean avgWordSearchServerExecTimeDebugSwitch) {
        this.avgWordSearchServerExecTimeDebugSwitch = avgWordSearchServerExecTimeDebugSwitch;
    }

    public boolean isWordSearchServerExecTimeDebugSwitch() {
        return wordSearchServerExecTimeDebugSwitch;
    }

    public void setWordSearchServerExecTimeDebugSwitch(
            boolean wordSearchServerExecTimeDebugSwitch) {
        this.wordSearchServerExecTimeDebugSwitch = wordSearchServerExecTimeDebugSwitch;
    }

    public boolean isRedisDebugSwitch() {
        return redisDebugSwitch;
    }

    public void setRedisDebugSwitch(boolean redisDebugSwitch) {
        this.redisDebugSwitch = redisDebugSwitch;
    }

    public boolean isBkHbaseDebugSwitch() {
        return bkHbaseDebugSwitch;
    }

    public void setBkHbaseDebugSwitch(boolean bkHbaseDebugSwitch) {
        this.bkHbaseDebugSwitch = bkHbaseDebugSwitch;
    }

}
