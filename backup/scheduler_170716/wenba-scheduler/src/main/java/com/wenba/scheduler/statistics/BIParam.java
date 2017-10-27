package com.wenba.scheduler.statistics;

import com.wenba.scheduler.AbstractParam;

/**
 * @author zhangbo
 *
 */
public class BIParam extends AbstractParam {

    // 成员变量
    /**
     * status(0 成功,1 失败)
     */
    private int status;

    /**
     * version(type+version)
     */
    private String version;

    /**
     * doc ids
     */
    private String docIds;

    /**
     * similarity
     */
    private float similarity;

    /**
     * ocr time
     */
    private long ocrTime;

    /**
     * handwrite time
     */
    private long handwriteTime;

    /**
     * img name
     */
    private String imgName;

    /**
     * search success or not
     */
    private boolean searchSuccess;

    /**
     * excute nlp or not
     */
    private boolean excuteNlp;

    /**
     * nlp time
     */
    private long nlpTime;

    /**
     * excute first search or not
     */
    private boolean excuteFirstSearch;

    /**
     * first search time
     */
    private long firstSearchTime;

    /**
     * excute second search or not
     */
    private boolean excuteSecondSearch;

    /**
     * second search time
     */
    private long secondSearchTime;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDocIds() {
        return docIds;
    }

    public void setDocIds(String docIds) {
        this.docIds = docIds;
    }

    public float getSimilarity() {
        return similarity;
    }

    public void setSimilarity(float similarity) {
        this.similarity = similarity;
    }

    public long getOcrTime() {
        return ocrTime;
    }

    public void setOcrTime(long ocrTime) {
        this.ocrTime = ocrTime;
    }

    public long getHandwriteTime() {
        return handwriteTime;
    }

    public void setHandwriteTime(long handwriteTime) {
        this.handwriteTime = handwriteTime;
    }

    public String getImgName() {
        return imgName;
    }

    public void setImgName(String imgName) {
        this.imgName = imgName;
    }

    public boolean isSearchSuccess() {
        return searchSuccess;
    }

    public void setSearchSuccess(boolean searchSuccess) {
        this.searchSuccess = searchSuccess;
    }

    public long getNlpTime() {
        return nlpTime;
    }

    public void setNlpTime(long nlpTime) {
        this.nlpTime = nlpTime;
    }

    public long getFirstSearchTime() {
        return firstSearchTime;
    }

    public void setFirstSearchTime(long firstSearchTime) {
        this.firstSearchTime = firstSearchTime;
    }

    public long getSecondSearchTime() {
        return secondSearchTime;
    }

    public void setSecondSearchTime(long secondSearchTime) {
        this.secondSearchTime = secondSearchTime;
    }

    public boolean isExcuteNlp() {
        return excuteNlp;
    }

    public void setExcuteNlp(boolean excuteNlp) {
        this.excuteNlp = excuteNlp;
    }

    public boolean isExcuteFirstSearch() {
        return excuteFirstSearch;
    }

    public void setExcuteFirstSearch(boolean excuteFirstSearch) {
        this.excuteFirstSearch = excuteFirstSearch;
    }

    public boolean isExcuteSecondSearch() {
        return excuteSecondSearch;
    }

    public void setExcuteSecondSearch(boolean excuteSecondSearch) {
        this.excuteSecondSearch = excuteSecondSearch;
    }

}
