package com.wenba.scheduler.search;

import org.springframework.beans.factory.FactoryBean;

import com.wenba.scheduler.ISchedulerStrategy;

/**
 * @author zhangbo
 *
 */
public class SearchHomeworkStrategyFactory implements
        FactoryBean<ISchedulerStrategy<SearchParam, SearchResult>> {

    /*
     * @return search homework strategy
     */
    public ISchedulerStrategy<SearchParam, SearchResult> getObject()
            throws Exception {
        return new SearchHomeworkStrategy();
    }

    /*
     * @return search homework strategy
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
