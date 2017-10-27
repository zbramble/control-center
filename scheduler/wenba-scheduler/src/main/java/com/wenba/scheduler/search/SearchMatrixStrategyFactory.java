package com.wenba.scheduler.search;

import org.springframework.beans.factory.FactoryBean;

import com.wenba.scheduler.ISchedulerStrategy;

/**
 * @author zhangbo
 *
 */
public class SearchMatrixStrategyFactory implements
        FactoryBean<ISchedulerStrategy<SearchParam, SearchResult>> {

    /*
     * @return search matrix strategy
     */
    public ISchedulerStrategy<SearchParam, SearchResult> getObject()
            throws Exception {
        return new SearchMatrixStrategy();
    }

    /*
     * @return search matrix strategy
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
