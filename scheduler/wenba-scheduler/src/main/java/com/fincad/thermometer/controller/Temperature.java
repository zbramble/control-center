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
public class Temperature {

    // member-variable
    /**
     * fahrenheit
     */
    private float fahrenheit;

    /**
     * celsius
     */
    private float celsius;

    // member-function
    /**
     * default constructor
     */
    public Temperature() {
        this(convertCelsiusToFahrenheit(0.0f), 0.0f);
    }

    /**
     * constructor
     * 
     * @param fahrenheit
     * @param celsius
     */
    public Temperature(float fahrenheit, float celsius) {
        this.fahrenheit = fahrenheit;
        this.celsius = celsius;
    }

    public static float convertCelsiusToFahrenheit(float celsius) {
        return (float) (1.8 * celsius + 32);
    }

    public static float convertFahrenheitToCelsius(float fahrenheit) {
        return (float) ((fahrenheit - 32) / 1.8);
    }

    public Float getFahrenheit() {
        return fahrenheit;
    }

    public void setFahrenheit(Float fahrenheit) {
        this.fahrenheit = fahrenheit;
    }

    public Float getCelsius() {
        return celsius;
    }

    public void setCelsius(Float celsius) {
        this.celsius = celsius;
    }

    /**
     * @author David
     *
     */
    public enum TemperatureType {
        FAHRENHEIT(1), CELSIUS(2);

        private final int value;

        public int getValue() {
            return value;
        }

        TemperatureType(int value) {
            this.value = value;
        }
    }

}
