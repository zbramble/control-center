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
public class TemperatureChangedObserver implements IObserver {

    // member-variable
    /**
     * freezing threshold
     */
    private float freezingThreshold;

    /**
     * boiling threshold
     */
    private float boilingThreshold;

    /**
     * freezing fluctuation
     */
    private float freezingFluctuation;

    /**
     * boiling fluctuation
     */
    private float boilingFluctuation;

    /**
     * freezing temperature direction
     */
    private TemperatureDirection freezingTemperatureDirection;

    /**
     * boiling temperature direction
     */
    private TemperatureDirection boilingTemperatureDirection;

    // member-function
    public TemperatureChangedObserver() {
        this(0.0f, 0.0f);
    }

    public TemperatureChangedObserver(float freezingThreshold,
            float boilingThreshold) {
        this.freezingThreshold = freezingThreshold;
        this.boilingThreshold = boilingThreshold;
    }

    public float getFreezingThreshold() {
        return freezingThreshold;
    }

    public void setFreezingThreshold(float freezingThreshold) {
        this.freezingThreshold = freezingThreshold;
    }

    public float getBoilingThreshold() {
        return boilingThreshold;
    }

    public void setBoilingThreshold(float boilingThreshold) {
        this.boilingThreshold = boilingThreshold;
    }

    public float getFreezingFluctuation() {
        return freezingFluctuation;
    }

    public void setFreezingFluctuation(float freezingFluctuation) {
        this.freezingFluctuation = freezingFluctuation;
    }

    public float getBoilingFluctuation() {
        return boilingFluctuation;
    }

    public void setBoilingFluctuation(float boilingFluctuation) {
        this.boilingFluctuation = boilingFluctuation;
    }

    public TemperatureDirection getFreezingTemperatureDirection() {
        return freezingTemperatureDirection;
    }

    public void setFreezingTemperatureDirection(
            TemperatureDirection freezingTemperatureDirection) {
        this.freezingTemperatureDirection = freezingTemperatureDirection;
    }

    public TemperatureDirection getBoilingTemperatureDirection() {
        return boilingTemperatureDirection;
    }

    public void setBoilingTemperatureDirection(
            TemperatureDirection boilingTemperatureDirection) {
        this.boilingTemperatureDirection = boilingTemperatureDirection;
    }

    public void update(String state) {
        System.out.println(toString());
    }

    public String toString() {
        return "12345";
    }

    /**
     * @author David
     *
     */
    public enum TemperatureDirection {
        INCREASE(1), DECREASE(2);

        private final int value;

        public int getValue() {
            return value;
        }

        TemperatureDirection(int value) {
            this.value = value;
        }
    }

}
