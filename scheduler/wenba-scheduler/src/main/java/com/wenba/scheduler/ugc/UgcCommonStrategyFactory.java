package com.wenba.scheduler.ugc;

import org.springframework.beans.factory.FactoryBean;

/**
 * @author zhangbo
 *
 */
public class UgcCommonStrategyFactory implements FactoryBean<UgcCommonStrategy> {

    /*
     * @return ugc common strategy
     */
    public UgcCommonStrategy getObject() throws Exception {
        return new UgcCommonStrategy();
    }

    /*
     * @return ugc common strategy
     */
    public Class<?> getObjectType() {
        return UgcCommonStrategy.class;
    }

    /*
     * @return singleton or not
     */
    public boolean isSingleton() {
        return true;
    }

}
