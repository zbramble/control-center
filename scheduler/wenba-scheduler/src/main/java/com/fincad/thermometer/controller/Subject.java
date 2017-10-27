package com.fincad.thermometer.controller;

import java.util.ArrayList;
import java.util.List;

/**
 * @author David
 *
 */
public abstract class Subject {

    // member-variable
    private List<IObserver> observerList;

    // member-function
    public Subject() {
        observerList = new ArrayList<IObserver>();
    }

    /**
     * register observer
     * 
     * @param observer
     *            IObserver
     */
    public void attach(IObserver observer) {

        observerList.add(observer);
        System.out.println("Attached an observer");
    }

    /**
     * delete observer
     * 
     * @param observer
     *            IObserver
     */
    public void detach(IObserver observer) {

        observerList.remove(observer);
        System.out.println("Detached an observer");
    }

    /**
     * notify observers
     */
    public void nodifyObservers(String newState) {

        for (IObserver observer : observerList) {
            observer.update(newState);
        }
    }

}
