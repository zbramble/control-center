package com.wenba.scheduler.ugc;

import org.springframework.beans.factory.FactoryBean;

import com.wenba.scheduler.ISchedulerStrategy;

/**
 * @author zhangbo
 *
 */
public class UgcHandwriteOcrStrategyFactory implements
        FactoryBean<ISchedulerStrategy<UgcParam, UgcResult>> {

    /*
     * @return ugc handwrite ocr strategy
     */
    public ISchedulerStrategy<UgcParam, UgcResult> getObject() throws Exception {
        return new UgcHandwriteOcrStrategy();
    }

    /*
     * @return ugc handwrite ocr strategy
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
