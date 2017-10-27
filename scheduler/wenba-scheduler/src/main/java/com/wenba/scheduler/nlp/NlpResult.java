package com.wenba.scheduler.nlp;

import com.wenba.scheduler.AbstractResult;

/**
 * @author zhangbo
 *
 */
public class NlpResult extends AbstractResult {

    /**
     * Nlp Failed Type
     */
    private NlpFailedType nlpFailedType;

    public NlpFailedType getNlpFailedType() {
        return nlpFailedType;
    }

    public void setNlpFailedType(NlpFailedType nlpFailedType) {
        this.nlpFailedType = nlpFailedType;
    }

    /**
     * Nlp Failed Type
     * 
     * @author zhangbo
     *
     */
    public enum NlpFailedType {
        NORMAL, NLP_FAIL, DATA_ERROR;
    }
}
