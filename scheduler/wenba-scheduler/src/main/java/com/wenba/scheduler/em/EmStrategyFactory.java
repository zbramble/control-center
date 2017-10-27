package com.wenba.scheduler.em;

import org.springframework.beans.factory.FactoryBean;

import com.wenba.scheduler.ISchedulerStrategy;

/**
 * @author zhangbo
 *
 */
public class EmStrategyFactory implements
        FactoryBean<ISchedulerStrategy<EmParam, EmResult>> {

    /*
     * @return em strategy
     */
    public ISchedulerStrategy<EmParam, EmResult> getObject() throws Exception {
        return new EmStrategy();
    }

    /*
     * @return em strategy
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
