package com.wenba.scheduler.statistics;

import com.wenba.scheduler.AbstractServer.ServerType;
import com.wenba.scheduler.nlp.NlpResult.NlpFailedType;
import com.wenba.scheduler.ocr.OcrResult.OcrFailedType;

/**
 * statistics sent to BI system
 * 
 * @author zhangbo
 *
 */
public class BIResultStatistics {

    // 成员变量
    /**
     * Scheduler ID
     */
    private String schedulerId;

    /**
     * feed ID
     */
    private String fid;

    /**
     * user ID
     */
    private String uid;

    /**
     * first ocr ID
     */
    private String firstOcrId;

    /**
     * first ocr type
     */
    private ServerType firstOcrType;

    /**
     * first ocr excute time
     */
    private long firstOcrExcuteTime;

    /**
     * first ocr failed type
     */
    private OcrFailedType firstOcrFailedType;

    /**
     * first ocr version
     */
    private String firstOcrVersion;

    /**
     * first nlp excute time
     */
    private long firstNlpExcuteTime;

    /**
     * first nlp failed type
     */
    private NlpFailedType firstNlpFailedType;

    /**
     * first nlp version
     */
    private String firstNlpVersion;

    /**
     * second ocr ID
     */
    private String secondOcrId;

    /**
     * second ocr type
     */
    private ServerType secondOcrType;

    /**
     * second ocr excute time
     */
    private long secondOcrExcuteTime;

    /**
     * second ocr failed type
     */
    private OcrFailedType secondOcrFailedType;

    /**
     * second ocr version
     */
    private String secondOcrVersion;

    /**
     * second nlp excute time
     */
    private long secondNlpExcuteTime;

    /**
     * second nlp failed type
     */
    private NlpFailedType secondNlpFailedType;

    /**
     * second nlp version
     */
    private String secondNlpVersion;

    /**
     * first search ID
     */
    private String firstSearchId;

    /**
     * first search excute time
     */
    private long firstSearchExcuteTime;

    /**
     * first search version
     */
    private String firstSearchVersion;

    /**
     * second search ID
     */
    private String secondSearchId;

    /**
     * second search excute time
     */
    private long secondSearchExcuteTime;

    /**
     * second search version
     */
    private String secondSearchVersion;

    /**
     * first similarity
     */
    private float firstSimilarity;

    /**
     * second similarity
     */
    private float secondSimilarity;

    /**
     * whether search no result or not
     */
    private boolean searchNoResult;

    public String getSchedulerId() {
        return schedulerId;
    }

    public void setSchedulerId(String schedulerId) {
        this.schedulerId = schedulerId;
    }

    public String getFid() {
        return fid;
    }

    public void setFid(String fid) {
        this.fid = fid;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getFirstOcrId() {
        return firstOcrId;
    }

    public void setFirstOcrId(String firstOcrId) {
        this.firstOcrId = firstOcrId;
    }

    public ServerType getFirstOcrType() {
        return firstOcrType;
    }

    public void setFirstOcrType(ServerType firstOcrType) {
        this.firstOcrType = firstOcrType;
    }

    public long getFirstOcrExcuteTime() {
        return firstOcrExcuteTime;
    }

    public void setFirstOcrExcuteTime(long firstOcrExcuteTime) {
        this.firstOcrExcuteTime = firstOcrExcuteTime;
    }

    public String getSecondOcrId() {
        return secondOcrId;
    }

    public void setSecondOcrId(String secondOcrId) {
        this.secondOcrId = secondOcrId;
    }

    public ServerType getSecondOcrType() {
        return secondOcrType;
    }

    public void setSecondOcrType(ServerType secondOcrType) {
        this.secondOcrType = secondOcrType;
    }

    public long getSecondOcrExcuteTime() {
        return secondOcrExcuteTime;
    }

    public void setSecondOcrExcuteTime(long secondOcrExcuteTime) {
        this.secondOcrExcuteTime = secondOcrExcuteTime;
    }

    public String getFirstSearchId() {
        return firstSearchId;
    }

    public void setFirstSearchId(String firstSearchId) {
        this.firstSearchId = firstSearchId;
    }

    public String getSecondSearchId() {
        return secondSearchId;
    }

    public void setSecondSearchId(String secondSearchId) {
        this.secondSearchId = secondSearchId;
    }

    public float getFirstSimilarity() {
        return firstSimilarity;
    }

    public void setFirstSimilarity(float firstSimilarity) {
        this.firstSimilarity = firstSimilarity;
    }

    public float getSecondSimilarity() {
        return secondSimilarity;
    }

    public void setSecondSimilarity(float secondSimilarity) {
        this.secondSimilarity = secondSimilarity;
    }

    public boolean isSearchNoResult() {
        return searchNoResult;
    }

    public void setSearchNoResult(boolean searchNoResult) {
        this.searchNoResult = searchNoResult;
    }

    public OcrFailedType getFirstOcrFailedType() {
        return firstOcrFailedType;
    }

    public void setFirstOcrFailedType(OcrFailedType firstOcrFailedType) {
        this.firstOcrFailedType = firstOcrFailedType;
    }

    public OcrFailedType getSecondOcrFailedType() {
        return secondOcrFailedType;
    }

    public void setSecondOcrFailedType(OcrFailedType secondOcrFailedType) {
        this.secondOcrFailedType = secondOcrFailedType;
    }

    public long getFirstSearchExcuteTime() {
        return firstSearchExcuteTime;
    }

    public void setFirstSearchExcuteTime(long firstSearchExcuteTime) {
        this.firstSearchExcuteTime = firstSearchExcuteTime;
    }

    public long getSecondSearchExcuteTime() {
        return secondSearchExcuteTime;
    }

    public void setSecondSearchExcuteTime(long secondSearchExcuteTime) {
        this.secondSearchExcuteTime = secondSearchExcuteTime;
    }

    public long getFirstNlpExcuteTime() {
        return firstNlpExcuteTime;
    }

    public void setFirstNlpExcuteTime(long firstNlpExcuteTime) {
        this.firstNlpExcuteTime = firstNlpExcuteTime;
    }

    public long getSecondNlpExcuteTime() {
        return secondNlpExcuteTime;
    }

    public void setSecondNlpExcuteTime(long secondNlpExcuteTime) {
        this.secondNlpExcuteTime = secondNlpExcuteTime;
    }

    public NlpFailedType getFirstNlpFailedType() {
        return firstNlpFailedType;
    }

    public void setFirstNlpFailedType(NlpFailedType firstNlpFailedType) {
        this.firstNlpFailedType = firstNlpFailedType;
    }

    public NlpFailedType getSecondNlpFailedType() {
        return secondNlpFailedType;
    }

    public void setSecondNlpFailedType(NlpFailedType secondNlpFailedType) {
        this.secondNlpFailedType = secondNlpFailedType;
    }

    public String getFirstOcrVersion() {
        return firstOcrVersion;
    }

    public void setFirstOcrVersion(String firstOcrVersion) {
        this.firstOcrVersion = firstOcrVersion;
    }

    public String getFirstNlpVersion() {
        return firstNlpVersion;
    }

    public void setFirstNlpVersion(String firstNlpVersion) {
        this.firstNlpVersion = firstNlpVersion;
    }

    public String getSecondOcrVersion() {
        return secondOcrVersion;
    }

    public void setSecondOcrVersion(String secondOcrVersion) {
        this.secondOcrVersion = secondOcrVersion;
    }

    public String getSecondNlpVersion() {
        return secondNlpVersion;
    }

    public void setSecondNlpVersion(String secondNlpVersion) {
        this.secondNlpVersion = secondNlpVersion;
    }

    public String getFirstSearchVersion() {
        return firstSearchVersion;
    }

    public void setFirstSearchVersion(String firstSearchVersion) {
        this.firstSearchVersion = firstSearchVersion;
    }

    public String getSecondSearchVersion() {
        return secondSearchVersion;
    }

    public void setSecondSearchVersion(String secondSearchVersion) {
        this.secondSearchVersion = secondSearchVersion;
    }

    /**
     * Statistics Type
     * 
     * @author zhangbo
     *
     */
    public enum StatisticsType {
        OCR_TIME(0), SEARCH_TIME(1), OCR_FAILED(2), OCR_SIMI(3), MULTI_OCR(4), SEARCH_NO_RESULT(
                5), CONN_COUNT(6), NLP_TIME(7), TIMEOUT(8), NLP_FAILED(9);

        private final int value;

        public int getValue() {
            return value;
        }

        StatisticsType(int value) {
            this.value = value;
        }
    }

}
