package com.wenba.scheduler.jzh;

import org.springframework.beans.factory.FactoryBean;

import com.wenba.scheduler.ISchedulerStrategy;

/**
 * @author zhangbo
 *
 */
public class JzhStrategyFactory implements
        FactoryBean<ISchedulerStrategy<JzhParam, JzhResult>> {

    /*
     * @return jzh strategy
     */
    public ISchedulerStrategy<JzhParam, JzhResult> getObject() throws Exception {
        return new JzhStrategy();
    }

    /*
     * @return jzh strategy
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
