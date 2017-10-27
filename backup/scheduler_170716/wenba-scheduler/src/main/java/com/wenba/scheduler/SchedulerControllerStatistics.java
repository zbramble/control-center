package com.wenba.scheduler;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author zhangbo
 *
 */
public class SchedulerControllerStatistics {

    // 成员变量
    /**
     * all request number
     */
    private AtomicLong allRequestNum;

    /**
     * current request number
     */
    private AtomicInteger currentRequestNum;

    /**
     * current cnn request number
     */
    private AtomicInteger currentCnnRequestNum;

    /**
     * current search request number
     */
    private AtomicInteger currentSearchRequestNum;

    /**
     * current http client request
     */
    private AtomicInteger currentHttpClientRequestNum;

    /**
     * exec time
     */
    private AtomicLong execTime;

    /**
     * exec time num
     */
    private AtomicInteger execTimeNum;

    /**
     * exec time by id
     */
    private AtomicLong execTimeById;

    /**
     * exec time by id num
     */
    private AtomicInteger execTimeByIdNum;

    /**
     * exec time by mining homework
     */
    private AtomicLong execTimeByHomework;

    /**
     * exec time by mining homework num
     */
    private AtomicInteger execTimeByHomeworkNum;

    /**
     * exec time by search
     */
    private AtomicLong execTimeBySearch;

    /**
     * exec time by search num
     */
    private AtomicInteger execTimeBySearchNum;

    /**
     * exec time by word search
     */
    private AtomicLong execTimeByWordSearch;

    /**
     * exec time by word search num
     */
    private AtomicInteger execTimeByWordSearchNum;

    /**
     * exec time by search article
     */
    private AtomicLong execTimeByArticle;

    /**
     * exec time by search article num
     */
    private AtomicInteger execTimeByArticleNum;

    /**
     * ocr hbase exec time
     */
    private AtomicLong ocrHbaseExecTime;

    /**
     * ocr hbase exec time num
     */
    private AtomicInteger ocrHbaseExecTimeNum;

    /**
     * search hbase exec time
     */
    private AtomicLong searchHbaseExecTime;

    /**
     * search hbase exec time num
     */
    private AtomicInteger searchHbaseExecTimeNum;

    /**
     * search article hbase exec time
     */
    private AtomicLong searchArticleHbaseExecTime;

    /**
     * search article hbase exec time num
     */
    private AtomicInteger searchArticleHbaseExecTimeNum;

    /**
     * img is null num
     */
    private AtomicInteger imgNullNum;

    /**
     * ocr hbase result num
     */
    private AtomicInteger ocrHbaseResultNum;

    /**
     * search hbase result num
     */
    private AtomicInteger searchHbaseResultNum;

    /**
     * search article hbase result num
     */
    private AtomicInteger searchArticleHbaseResultNum;

    /**
     * migu ocr hbase result num
     */
    private AtomicInteger miguOcrHbaseResultNum;

    /**
     * migu search hbase result num
     */
    private AtomicInteger miguSearchHbaseResultNum;

    /**
     * sdk ocr hbase result num
     */
    private AtomicInteger sdkOcrHbaseResultNum;

    /**
     * sdk search hbase result num
     */
    private AtomicInteger sdkSearchHbaseResultNum;

    public SchedulerControllerStatistics() {
        allRequestNum = new AtomicLong(0);
        currentRequestNum = new AtomicInteger(0);
        currentCnnRequestNum = new AtomicInteger(0);
        currentSearchRequestNum = new AtomicInteger(0);
        currentHttpClientRequestNum = new AtomicInteger(0);
        execTime = new AtomicLong(0);
        execTimeNum = new AtomicInteger(0);
        execTimeById = new AtomicLong(0);
        execTimeByIdNum = new AtomicInteger(0);
        execTimeByHomework = new AtomicLong(0);
        execTimeByHomeworkNum = new AtomicInteger(0);
        execTimeBySearch = new AtomicLong(0);
        execTimeBySearchNum = new AtomicInteger(0);
        execTimeByWordSearch = new AtomicLong(0);
        execTimeByWordSearchNum = new AtomicInteger(0);
        execTimeByArticle = new AtomicLong(0);
        execTimeByArticleNum = new AtomicInteger(0);
        ocrHbaseExecTime = new AtomicLong(0);
        ocrHbaseExecTimeNum = new AtomicInteger(0);
        searchHbaseExecTime = new AtomicLong(0);
        searchHbaseExecTimeNum = new AtomicInteger(0);
        searchArticleHbaseExecTime = new AtomicLong(0);
        searchArticleHbaseExecTimeNum = new AtomicInteger(0);
        imgNullNum = new AtomicInteger(0);
        ocrHbaseResultNum = new AtomicInteger(0);
        searchHbaseResultNum = new AtomicInteger(0);
        searchArticleHbaseResultNum = new AtomicInteger(0);
        miguOcrHbaseResultNum = new AtomicInteger(0);
        miguSearchHbaseResultNum = new AtomicInteger(0);
        sdkOcrHbaseResultNum = new AtomicInteger(0);
        sdkSearchHbaseResultNum = new AtomicInteger(0);
    }

    public int getSearchArticleHbaseResultNum() {
        return searchArticleHbaseResultNum.get();
    }

    public void setSearchArticleHbaseResultNum(int searchArticleHbaseResultNum) {
        this.searchArticleHbaseResultNum.set(searchArticleHbaseResultNum);
    }

    public int incrementAndGetSearchArticleHbaseResultNum() {
        return searchArticleHbaseResultNum.incrementAndGet();
    }

    public int decrementAndGetSearchArticleHbaseResultNum() {
        return searchArticleHbaseResultNum.decrementAndGet();
    }

    public int getSearchHbaseResultNum() {
        return searchHbaseResultNum.get();
    }

    public void setSearchHbaseResultNum(int searchHbaseResultNum) {
        this.searchHbaseResultNum.set(searchHbaseResultNum);
    }

    public int incrementAndGetSearchHbaseResultNum() {
        return searchHbaseResultNum.incrementAndGet();
    }

    public int decrementAndGetSearchHbaseResultNum() {
        return searchHbaseResultNum.decrementAndGet();
    }

    public int getOcrHbaseResultNum() {
        return ocrHbaseResultNum.get();
    }

    public void setOcrHbaseResultNum(int ocrHbaseResultNum) {
        this.ocrHbaseResultNum.set(ocrHbaseResultNum);
    }

    public int incrementAndGetOcrHbaseResultNum() {
        return ocrHbaseResultNum.incrementAndGet();
    }

    public int decrementAndGetOcrHbaseResultNum() {
        return ocrHbaseResultNum.decrementAndGet();
    }

    public int getSearchHbaseExecTimeNum() {
        return searchHbaseExecTimeNum.get();
    }

    public void setSearchHbaseExecTimeNum(int searchHbaseExecTimeNum) {
        this.searchHbaseExecTimeNum.set(searchHbaseExecTimeNum);
    }

    public int incrementAndGetSearchHbaseExecTimeNum() {
        return searchHbaseExecTimeNum.incrementAndGet();
    }

    public int getSearchArticleHbaseExecTimeNum() {
        return searchArticleHbaseExecTimeNum.get();
    }

    public void setSearchArticleHbaseExecTimeNum(
            int searchArticleHbaseExecTimeNum) {
        this.searchArticleHbaseExecTimeNum.set(searchArticleHbaseExecTimeNum);
    }

    public int incrementAndGetSearchArticleHbaseExecTimeNum() {
        return searchArticleHbaseExecTimeNum.incrementAndGet();
    }

    public int getOcrHbaseExecTimeNum() {
        return ocrHbaseExecTimeNum.get();
    }

    public void setOcrHbaseExecTimeNum(int ocrHbaseExecTimeNum) {
        this.ocrHbaseExecTimeNum.set(ocrHbaseExecTimeNum);
    }

    public int incrementAndGetOcrHbaseExecTimeNum() {
        return ocrHbaseExecTimeNum.incrementAndGet();
    }

    public long getSearchHbaseExecTime() {
        return searchHbaseExecTime.get();
    }

    public void setSearchHbaseExecTime(long searchHbaseExecTime) {
        this.searchHbaseExecTime.set(searchHbaseExecTime);
    }

    public long addAndGetSearchHbaseExecTime(long searchHbaseExecTime) {
        return this.searchHbaseExecTime.addAndGet(searchHbaseExecTime);
    }

    public long getSearchArticleHbaseExecTime() {
        return searchArticleHbaseExecTime.get();
    }

    public void setSearchArticleHbaseExecTime(long searchArticleHbaseExecTime) {
        this.searchArticleHbaseExecTime.set(searchArticleHbaseExecTime);
    }

    public long addAndGetSearchArticleHbaseExecTime(
            long searchArticleHbaseExecTime) {
        return this.searchArticleHbaseExecTime
                .addAndGet(searchArticleHbaseExecTime);
    }

    public long getOcrHbaseExecTime() {
        return ocrHbaseExecTime.get();
    }

    public void setOcrHbaseExecTime(long ocrHbaseExecTime) {
        this.ocrHbaseExecTime.set(ocrHbaseExecTime);
    }

    public long addAndGetOcrHbaseExecTime(long ocrHbaseExecTime) {
        return this.ocrHbaseExecTime.addAndGet(ocrHbaseExecTime);
    }

    public int getExecTimeByIdNum() {
        return execTimeByIdNum.get();
    }

    public void setExecTimeByIdNum(int execTimeByIdNum) {
        this.execTimeByIdNum.set(execTimeByIdNum);
    }

    public int incrementAndGetExecTimeByIdNum() {
        return execTimeByIdNum.incrementAndGet();
    }

    public long getExecTimeById() {
        return execTimeById.get();
    }

    public void setExecTimeById(long execTimeById) {
        this.execTimeById.set(execTimeById);
    }

    public long addAndGetExecTimeById(long execTimeById) {
        return this.execTimeById.addAndGet(execTimeById);
    }

    public long getExecTimeByHomework() {
        return execTimeByHomework.get();
    }

    public void setExecTimeByHomework(long execTimeByHomework) {
        this.execTimeByHomework.set(execTimeByHomework);
    }

    public long addAndGetExecTimeByHomework(long execTimeByHomework) {
        return this.execTimeByHomework.addAndGet(execTimeByHomework);
    }

    public int getExecTimeByHomeworkNum() {
        return execTimeByHomeworkNum.get();
    }

    public void setExecTimeByHomeworkNum(int execTimeByHomeworkNum) {
        this.execTimeByHomeworkNum.set(execTimeByHomeworkNum);
    }

    public int incrementAndGetExecTimeByHomeworkNum() {
        return execTimeByHomeworkNum.incrementAndGet();
    }

    public long getExecTimeBySearch() {
        return execTimeBySearch.get();
    }

    public void setExecTimeBySearch(long execTimeBySearch) {
        this.execTimeBySearch.set(execTimeBySearch);
    }

    public long addAndGetExecTimeBySearch(long execTimeBySearch) {
        return this.execTimeBySearch.addAndGet(execTimeBySearch);
    }

    public int getExecTimeBySearchNum() {
        return execTimeBySearchNum.get();
    }

    public void setExecTimeBySearchNum(int execTimeBySearchNum) {
        this.execTimeBySearchNum.set(execTimeBySearchNum);
    }

    public int incrementAndGetExecTimeBySearchNum() {
        return execTimeBySearchNum.incrementAndGet();
    }

    public long getExecTimeByWordSearch() {
        return execTimeByWordSearch.get();
    }

    public void setExecTimeByWordSearch(long execTimeByWordSearch) {
        this.execTimeByWordSearch.set(execTimeByWordSearch);
    }

    public long addAndGetExecTimeByWordSearch(long execTimeByWordSearch) {
        return this.execTimeByWordSearch.addAndGet(execTimeByWordSearch);
    }

    public int getExecTimeByWordSearchNum() {
        return execTimeByWordSearchNum.get();
    }

    public void setExecTimeByWordSearchNum(int execTimeByWordSearchNum) {
        this.execTimeByWordSearchNum.set(execTimeByWordSearchNum);
    }

    public int incrementAndGetExecTimeByWordSearchNum() {
        return execTimeByWordSearchNum.incrementAndGet();
    }

    public long getExecTimeByArticle() {
        return execTimeByArticle.get();
    }

    public void setExecTimeByArticle(long execTimeByArticle) {
        this.execTimeByArticle.set(execTimeByArticle);
    }

    public long addAndGetExecTimeByArticle(long execTimeByArticle) {
        return this.execTimeByArticle.addAndGet(execTimeByArticle);
    }

    public int getExecTimeByArticleNum() {
        return execTimeByArticleNum.get();
    }

    public void setExecTimeByArticleNum(int execTimeByArticleNum) {
        this.execTimeByArticleNum.set(execTimeByArticleNum);
    }

    public int incrementAndGetExecTimeByArticleNum() {
        return execTimeByArticleNum.incrementAndGet();
    }

    public int getImgNullNum() {
        return imgNullNum.get();
    }

    public void setImgNullNum(int imgNullNum) {
        this.imgNullNum.set(imgNullNum);
    }

    public int incrementAndGetImgNullNum() {
        return imgNullNum.incrementAndGet();
    }

    public int getExecTimeNum() {
        return execTimeNum.get();
    }

    public void setExecTimeNum(int execTimeNum) {
        this.execTimeNum.set(execTimeNum);
    }

    public int incrementAndGetExecTimeNum() {
        return execTimeNum.incrementAndGet();
    }

    public long getExecTime() {
        return execTime.get();
    }

    public void setExecTime(long execTime) {
        this.execTime.set(execTime);
    }

    public long addAndGetExecTime(long execTime) {
        return this.execTime.addAndGet(execTime);
    }

    public long getAllRequestNum() {
        return allRequestNum.get();
    }

    public void setAllRequestNum(long allRequestNum) {
        this.allRequestNum.set(allRequestNum);
    }

    public long incrementAndGetAllRequestNum() {
        return allRequestNum.incrementAndGet();
    }

    public int getCurrentRequestNum() {
        return currentRequestNum.get();
    }

    public void setCurrentRequestNum(int currentRequestNum) {
        this.currentRequestNum.set(currentRequestNum);
    }

    public int incrementAndGetCurrentRequestNum() {
        return currentRequestNum.incrementAndGet();
    }

    public int decrementAndGetCurrentRequestNum() {
        return currentRequestNum.decrementAndGet();
    }

    public int getCurrentCnnRequestNum() {
        return currentCnnRequestNum.get();
    }

    public void setCurrentCnnRequestNum(int currentCnnRequestNum) {
        this.currentCnnRequestNum.set(currentCnnRequestNum);
    }

    public int incrementAndGetCurrentCnnRequestNum() {
        return currentCnnRequestNum.incrementAndGet();
    }

    public int decrementAndGetCurrentCnnRequestNum() {
        return currentCnnRequestNum.decrementAndGet();
    }

    public int getCurrentSearchRequestNum() {
        return currentSearchRequestNum.get();
    }

    public void setCurrentSearchRequestNum(int currentSearchRequestNum) {
        this.currentSearchRequestNum.set(currentSearchRequestNum);
    }

    public int incrementAndGetCurrentSearchRequestNum() {
        return currentSearchRequestNum.incrementAndGet();
    }

    public int decrementAndGetCurrentSearchRequestNum() {
        return currentSearchRequestNum.decrementAndGet();
    }

    public int getCurrentHttpClientRequestNum() {
        return currentHttpClientRequestNum.get();
    }

    public void setCurrentHttpClientRequestNum(int currentHttpClientRequestNum) {
        this.currentHttpClientRequestNum.set(currentHttpClientRequestNum);
    }

    public int incrementAndGetCurrentHttpClientRequestNum() {
        return currentHttpClientRequestNum.incrementAndGet();
    }

    public int decrementAndGetCurrentHttpClientRequestNum() {
        return currentHttpClientRequestNum.decrementAndGet();
    }

    public int getMiguSearchHbaseResultNum() {
        return miguSearchHbaseResultNum.get();
    }

    public void setMiguSearchHbaseResultNum(int miguSearchHbaseResultNum) {
        this.miguSearchHbaseResultNum.set(miguSearchHbaseResultNum);
    }

    public int incrementAndGetMiguSearchHbaseResultNum() {
        return miguSearchHbaseResultNum.incrementAndGet();
    }

    public int decrementAndGetMiguSearchHbaseResultNum() {
        return miguSearchHbaseResultNum.decrementAndGet();
    }

    public int getMiguOcrHbaseResultNum() {
        return miguOcrHbaseResultNum.get();
    }

    public void setMiguOcrHbaseResultNum(int miguOcrHbaseResultNum) {
        this.miguOcrHbaseResultNum.set(miguOcrHbaseResultNum);
    }

    public int incrementAndGetMiguOcrHbaseResultNum() {
        return miguOcrHbaseResultNum.incrementAndGet();
    }

    public int decrementAndGetMiguOcrHbaseResultNum() {
        return miguOcrHbaseResultNum.decrementAndGet();
    }

    public int getSdkSearchHbaseResultNum() {
        return sdkSearchHbaseResultNum.get();
    }

    public void setSdkSearchHbaseResultNum(int sdkSearchHbaseResultNum) {
        this.sdkSearchHbaseResultNum.set(sdkSearchHbaseResultNum);
    }

    public int incrementAndGetSdkSearchHbaseResultNum() {
        return sdkSearchHbaseResultNum.incrementAndGet();
    }

    public int decrementAndGetSdkSearchHbaseResultNum() {
        return sdkSearchHbaseResultNum.decrementAndGet();
    }

    public int getSdkOcrHbaseResultNum() {
        return sdkOcrHbaseResultNum.get();
    }

    public void setSdkOcrHbaseResultNum(int sdkOcrHbaseResultNum) {
        this.sdkOcrHbaseResultNum.set(sdkOcrHbaseResultNum);
    }

    public int incrementAndGetSdkOcrHbaseResultNum() {
        return sdkOcrHbaseResultNum.incrementAndGet();
    }

    public int decrementAndGetSdkOcrHbaseResultNum() {
        return sdkOcrHbaseResultNum.decrementAndGet();
    }

}
