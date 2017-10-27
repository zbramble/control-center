package com.wenba.scheduler.statistics;

import org.springframework.beans.factory.FactoryBean;

/**
 * @author zhangbo
 *
 */
public class BIStrategyFactory implements FactoryBean<BIStrategy> {

    /*
     * @return bi strategy
     */
    public BIStrategy getObject() throws Exception {
        return new BIStrategy();
    }

    /*
     * @return bi strategy
     */
    public Class<?> getObjectType() {
        return BIStrategy.class;
    }

    /*
     * @return singleton or not
     */
    public boolean isSingleton() {
        return true;
    }

}
