package com.wenba.scheduler.config;

import org.springframework.beans.factory.FactoryBean;

import com.wenba.scheduler.ISchedulerStrategy;

/**
 * @author zhangbo
 *
 */
public class ConfigStrategyFactory implements
        FactoryBean<ISchedulerStrategy<ConfigParam, ConfigResult>> {

    /*
     * @return config strategy
     */
    public ISchedulerStrategy<ConfigParam, ConfigResult> getObject()
            throws Exception {
        return new ConfigStrategy();
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
