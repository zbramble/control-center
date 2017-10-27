package com.wenba.scheduler.nlp;

import com.wenba.scheduler.AbstractServer;

/**
 * @author zhangbo
 *
 */
public class NlpServer extends AbstractServer {

    // 成员变量
    private NlpServerStatistics nlpServerStatistics;

    public NlpServerStatistics getNlpServerStatistics() {
        return nlpServerStatistics;
    }

    public void setNlpServerStatistics(NlpServerStatistics nlpServerStatistics) {
        this.nlpServerStatistics = nlpServerStatistics;
    }

}
