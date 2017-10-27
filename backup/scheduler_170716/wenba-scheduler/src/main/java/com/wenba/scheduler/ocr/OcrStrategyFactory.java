package com.wenba.scheduler.ocr;

import org.springframework.beans.factory.FactoryBean;

import com.wenba.scheduler.ISchedulerStrategy;

/**
 * @author zhangbo
 *
 */
public class OcrStrategyFactory implements
        FactoryBean<ISchedulerStrategy<OcrParam, OcrResult>> {

    /*
     * @return ocr strategy
     */
    public ISchedulerStrategy<OcrParam, OcrResult> getObject() throws Exception {
        return new OcrStrategy();
    }

    /*
     * @return ocr strategy
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
