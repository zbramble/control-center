package com.wenba.scheduler.config;

import org.springframework.beans.factory.FactoryBean;

import com.wenba.scheduler.ISchedulerStrategy;

/**
 * @author zhangbo
 *
 */
public class ConfigFromHtmlStrategyFactory implements
        FactoryBean<ISchedulerStrategy<ConfigParam, ConfigResult>> {

    /*
     * @return config strategy from html
     */
    public ISchedulerStrategy<ConfigParam, ConfigResult> getObject()
            throws Exception {
        return new ConfigFromHtmlStrategy();
    }

    /*
     * @return config strategy
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
