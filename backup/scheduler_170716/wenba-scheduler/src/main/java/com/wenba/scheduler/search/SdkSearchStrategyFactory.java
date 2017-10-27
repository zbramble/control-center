package com.wenba.scheduler.search;

import org.springframework.beans.factory.FactoryBean;

import com.wenba.scheduler.ISchedulerStrategy;

/**
 * @author zhangbo
 *
 */
public class SdkSearchStrategyFactory implements
        FactoryBean<ISchedulerStrategy<SearchParam, SearchResult>> {

    /*
     * @return sdk search strategy
     */
    public ISchedulerStrategy<SearchParam, SearchResult> getObject()
            throws Exception {
        return new SdkSearchStrategy();
    }

    /*
     * @return sdk search strategy
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
