package com.wenba.scheduler.ocr;

import net.sf.json.JSONObject;

/**
 * @author zhangbo
 *
 */
public class OcrJsonResult {

    /**
     * data
     */
    private String data;

    /**
     * layoutinfo
     */
    private JSONObject layoutinfo;

    /**
     * layouttime
     */
    private double layouttime;

    /**
     * nlpdata
     */
    private String nlpdata;

    /**
     * recogtime
     */
    private double recogtime;

    /**
     * rotate
     */
    private int rotate;

    /**
     * status
     */
    private int status;

    /**
     * type
     */
    private String type;

    /**
     * version
     */
    private String version;

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public JSONObject getLayoutinfo() {
        return layoutinfo;
    }

    public void setLayoutinfo(JSONObject layoutinfo) {
        this.layoutinfo = layoutinfo;
    }

    public double getLayouttime() {
        return layouttime;
    }

    public void setLayouttime(double layouttime) {
        this.layouttime = layouttime;
    }

    public String getNlpdata() {
        return nlpdata;
    }

    public void setNlpdata(String nlpdata) {
        this.nlpdata = nlpdata;
    }

    public double getRecogtime() {
        return recogtime;
    }

    public void setRecogtime(double recogtime) {
        this.recogtime = recogtime;
    }

    public int getRotate() {
        return rotate;
    }

    public void setRotate(int rotate) {
        this.rotate = rotate;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

}
