package com.fincad.thermometer.controller;

/**
 * @file
 * @brief
 * @author David
 * @date
 * @version
 * @note
 *
 */
public class TemperatureChangedSubject extends Subject {

    // member-variable
    /**
     * temperature
     */
    private Temperature temperature;

    // member-function
    public TemperatureChangedSubject() {
        this(new Temperature());
    }

    public TemperatureChangedSubject(Temperature temperature) {
        this.temperature = temperature;
    }

    public Temperature getTemperature() {
        return temperature;
    }

    public void setTemperature(Temperature temperature) {
        this.temperature = temperature;
    }

    public void change(String newState) {

        temperature = new Temperature(
                Temperature
                        .convertCelsiusToFahrenheit(Float.parseFloat(newState)),
                Float.parseFloat(newState));
        // System.out.println("temperature is：" + state);
        // temperature changed, notify every observers
        this.nodifyObservers(newState);
    }

}
