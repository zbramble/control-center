package com.wenba.scheduler.search;

import net.sf.json.JSONArray;

/**
 * @author zhangbo
 *
 */
public class SearchJsonResult {

    /**
     * type
     */
    private String type;

    /**
     * keywords
     */
    private String keywords;

    /**
     * time
     */
    private double time;

    /**
     * questions
     */
    private JSONArray questions;

    /**
     * version
     */
    private String version;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public double getTime() {
        return time;
    }

    public void setTime(double time) {
        this.time = time;
    }

    public JSONArray getQuestions() {
        return questions;
    }

    public void setQuestions(JSONArray questions) {
        this.questions = questions;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

}
