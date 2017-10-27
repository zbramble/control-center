package com.wenba.scheduler.ocr;

/**
 * @author zhangbo
 *
 */
public class OcrHbaseResult extends OcrResult {

    // 成员变量
    /**
     * feed ID
     */
    private long fid;

    /**
     * user ID
     */
    private int uid;

    /**
     * ocr server for excute ocr
     */
    private OcrServer ocrServer;

    public long getFid() {
        return fid;
    }

    public void setFid(long fid) {
        this.fid = fid;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public OcrServer getOcrServer() {
        return ocrServer;
    }

    public void setOcrServer(OcrServer ocrServer) {
        this.ocrServer = ocrServer;
    }

}
