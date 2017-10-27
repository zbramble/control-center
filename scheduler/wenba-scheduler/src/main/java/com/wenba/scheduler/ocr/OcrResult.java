package com.wenba.scheduler.ocr;

import com.wenba.scheduler.AbstractResult;
import com.wenba.scheduler.AbstractServer.ServerType;
import com.wenba.scheduler.nlp.NlpResult.NlpFailedType;

/**
 * @author zhangbo
 *
 */
public class OcrResult extends AbstractResult {

    /**
     * Ocr Failed Type
     */
    private OcrFailedType ocrFailedType;

    /**
     * ocr type
     */
    private ServerType ocrType;

    /**
     * excute nlp or not
     */
    private boolean excuteNlp;

    /**
     * nlp excute time
     */
    private long nlpExcuteTime;

    /**
     * nlp Failed Type
     */
    private NlpFailedType nlpFailedType;

    /**
     * nlp version
     */
    private String nlpVersion;

    /**
     * rotate
     */
    private int rotate;

    /**
     * layoutinfo
     */
    private String layoutinfo;

    /**
     * ocr & nlp excute time
     */
    private long ocrAndNlpExcuteTime;

    public OcrFailedType getOcrFailedType() {
        return ocrFailedType;
    }

    public void setOcrFailedType(OcrFailedType ocrFailedType) {
        this.ocrFailedType = ocrFailedType;
    }

    public ServerType getOcrType() {
        return ocrType;
    }

    public void setOcrType(ServerType ocrType) {
        this.ocrType = ocrType;
    }

    public boolean isExcuteNlp() {
        return excuteNlp;
    }

    public void setExcuteNlp(boolean excuteNlp) {
        this.excuteNlp = excuteNlp;
    }

    public long getNlpExcuteTime() {
        return nlpExcuteTime;
    }

    public void setNlpExcuteTime(long nlpExcuteTime) {
        this.nlpExcuteTime = nlpExcuteTime;
    }

    public NlpFailedType getNlpFailedType() {
        return nlpFailedType;
    }

    public void setNlpFailedType(NlpFailedType nlpFailedType) {
        this.nlpFailedType = nlpFailedType;
    }

    public String getNlpVersion() {
        return nlpVersion;
    }

    public void setNlpVersion(String nlpVersion) {
        this.nlpVersion = nlpVersion;
    }

    public int getRotate() {
        return rotate;
    }

    public void setRotate(int rotate) {
        this.rotate = rotate;
    }

    public String getLayoutinfo() {
        return layoutinfo;
    }

    public void setLayoutinfo(String layoutinfo) {
        this.layoutinfo = layoutinfo;
    }

    public long getOcrAndNlpExcuteTime() {
        return ocrAndNlpExcuteTime;
    }

    public void setOcrAndNlpExcuteTime(long ocrAndNlpExcuteTime) {
        this.ocrAndNlpExcuteTime = ocrAndNlpExcuteTime;
    }

    /**
     * Ocr Failed Type
     * 
     * @author zhangbo
     *
     */
    public enum OcrFailedType {
        NORMAL(0), IMG(1), OCR(2);

        private final int value;

        public int getValue() {
            return value;
        }

        OcrFailedType(int value) {
            this.value = value;
        }
    }

}
