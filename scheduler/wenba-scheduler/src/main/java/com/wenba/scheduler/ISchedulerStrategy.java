package com.wenba.scheduler;

/**
 * @author zhangbo
 *
 * @param <T1>
 *            AbstractSchedulerParam及其子类
 * @param <T2>
 *            AbstractSchedulerResult及其子类
 * 
 */
public interface ISchedulerStrategy<T1 extends AbstractParam, T2 extends AbstractResult> {
    T2 excute(T1 schedulerParam);
}
