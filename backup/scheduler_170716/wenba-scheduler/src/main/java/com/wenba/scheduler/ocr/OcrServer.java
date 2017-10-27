package com.wenba.scheduler.ocr;

import com.wenba.scheduler.AbstractServer;

/**
 * @author zhangbo
 *
 */
public class OcrServer extends AbstractServer {

    // 成员变量
    private OcrServerStatistics ocrServerStatistics;

    public OcrServerStatistics getOcrServerStatistics() {
        return ocrServerStatistics;
    }

    public void setOcrServerStatistics(OcrServerStatistics ocrServerStatistics) {
        this.ocrServerStatistics = ocrServerStatistics;
    }

}
