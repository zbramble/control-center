package com.wenba.scheduler.nlp;

import org.springframework.beans.factory.FactoryBean;

import com.wenba.scheduler.ISchedulerStrategy;

/**
 * @author zhangbo
 *
 */
public class NlpStrategyFactory implements
        FactoryBean<ISchedulerStrategy<NlpParam, NlpResult>> {

    /*
     * @return ocr strategy
     */
    public ISchedulerStrategy<NlpParam, NlpResult> getObject() throws Exception {
        return new NlpStrategy();
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
