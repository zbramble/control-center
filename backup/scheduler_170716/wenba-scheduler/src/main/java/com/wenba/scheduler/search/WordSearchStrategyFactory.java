package com.wenba.scheduler.search;

import org.springframework.beans.factory.FactoryBean;

import com.wenba.scheduler.ISchedulerStrategy;

/**
 * @author zhangbo
 *
 */
public class WordSearchStrategyFactory implements
        FactoryBean<ISchedulerStrategy<SearchParam, SearchResult>> {

    /*
     * @return word search strategy
     */
    public ISchedulerStrategy<SearchParam, SearchResult> getObject()
            throws Exception {
        return new WordSearchStrategy();
    }

    /*
     * @return word search strategy
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
