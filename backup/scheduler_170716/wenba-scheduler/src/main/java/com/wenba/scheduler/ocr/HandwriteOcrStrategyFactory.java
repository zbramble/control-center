package com.wenba.scheduler.ocr;

import org.springframework.beans.factory.FactoryBean;

import com.wenba.scheduler.ISchedulerStrategy;

/**
 * @author zhangbo
 *
 */
public class HandwriteOcrStrategyFactory implements
        FactoryBean<ISchedulerStrategy<OcrParam, OcrResult>> {

    /*
     * @return handwrite ocr strategy
     */
    public ISchedulerStrategy<OcrParam, OcrResult> getObject() throws Exception {
        return new HandwriteOcrStrategy();
    }

    /*
     * @return handwrite ocr strategy
     */
    public Class<?> getObjectType() {
        return ISchedulerStrategy.class;
    }

    /*
     * @return singleton or not
     */
    public boolean isSingleton() {
        return true;
    }

}
