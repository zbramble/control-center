package com.wenba.scheduler.nlp;

import com.wenba.scheduler.AbstractParam;
import com.wenba.scheduler.AbstractResult;

/**
 * @author zhangbo
 *
 */
public class NlpParam extends AbstractParam {

    // 成员变量
    /**
     * nlp server for excute nlp
     */
    private NlpServer nlpServer;

    /**
     * ocr result
     */
    private AbstractResult ocrNlpResult;

    /**
     * ocr excute time
     */
    private long ocrExcuteTime;

    public NlpServer getNlpServer() {
        return nlpServer;
    }

    public void setNlpServer(NlpServer nlpServer) {
        this.nlpServer = nlpServer;
    }

    public AbstractResult getOcrNlpResult() {
        return ocrNlpResult;
    }

    public void setOcrNlpResult(AbstractResult ocrNlpResult) {
        this.ocrNlpResult = ocrNlpResult;
    }

    public long getOcrExcuteTime() {
        return ocrExcuteTime;
    }

    public void setOcrExcuteTime(long ocrExcuteTime) {
        this.ocrExcuteTime = ocrExcuteTime;
    }

}
