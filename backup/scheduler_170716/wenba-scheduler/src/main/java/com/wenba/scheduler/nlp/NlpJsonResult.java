package com.wenba.scheduler.nlp;

/**
 * @author zhangbo
 *
 */
public class NlpJsonResult {

    /**
     * nlpdata
     */
    private String nlpdata;

    /**
     * status
     */
    private int status;

    /**
     * version
     */
    private String version;

    public String getNlpdata() {
        return nlpdata;
    }

    public void setNlpdata(String nlpdata) {
        this.nlpdata = nlpdata;
    }

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

}
