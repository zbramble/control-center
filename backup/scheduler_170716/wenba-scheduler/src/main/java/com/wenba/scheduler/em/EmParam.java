package com.wenba.scheduler.em;

import net.sf.json.JSONObject;

import com.wenba.scheduler.AbstractParam;

/**
 * @author zhangbo
 *
 */
public class EmParam extends AbstractParam {

    // 成员变量
    /**
     * datum
     */
    private JSONObject datum;

    /**
     * em server for excute em
     */
    private EmServer emServer;

    public JSONObject getDatum() {
        return datum;
    }

    public void setDatum(JSONObject datum) {
        this.datum = datum;
    }

    public EmServer getEmServer() {
        return emServer;
    }

    public void setEmServer(EmServer emServer) {
        this.emServer = emServer;
    }

}
