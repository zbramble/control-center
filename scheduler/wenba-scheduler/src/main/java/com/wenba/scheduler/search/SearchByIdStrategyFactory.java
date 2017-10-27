package com.wenba.scheduler.search;

import org.springframework.beans.factory.FactoryBean;

import com.wenba.scheduler.ISchedulerStrategy;

/**
 * @author zhangbo
 *
 */
public class SearchByIdStrategyFactory implements
        FactoryBean<ISchedulerStrategy<SearchParam, SearchResult>> {

    /*
     * @return search by id strategy
     */
    public ISchedulerStrategy<SearchParam, SearchResult> getObject()
            throws Exception {
        return new SearchByIdStrategy();
    }

    /*
     * @return search by id strategy
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
