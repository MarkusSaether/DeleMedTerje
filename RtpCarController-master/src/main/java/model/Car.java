package model;

import util.Protocol;

import java.util.Observable;

public class Car extends Observable {
    private boolean active;
    private Throttle throttle;
    private int steer;

    /**
     * Creates a new Car.
     */
    public Car() {
        active = false;
    }


    // -----------------------------------------------------------------------------------------------------------------
    // GETTERS
    // -----------------------------------------------------------------------------------------------------------------

    public boolean isActive() {
        return active;
    }

    public Throttle getThrottle() {
        return throttle;
    }

    public int getSteer() {
        return steer;
    }


    // -----------------------------------------------------------------------------------------------------------------
    // CAR CONTROL
    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Sets the active status of this Car.
     * @param active True if the Car's status should be active, false otherwise.
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * Throttles the car in the given direction, or neutralises throttle.
     * @param dir The direction to throttle in (Throttle.NEUTRAL, Throttle.FORWARD, or Throttle.NEUTRAL).
     */
    public void throttle(Throttle dir) {
        throttle = dir;
        setChanged();
        notifyObservers();
    }

    /**
     * Steers the car into the given direction.
     * @param angle The direction to steer towards, given as an angle in degrees.
     */
    public void steer(int angle) {
        steer = angle;
        setChanged();
        notifyObservers();
    }

    /**
     * Steers the car into the given direction.
     * @param dir The direction to steer towards, being Steer.NEUTRAL, Steer.LEFT, or Steer.RIGHT.
     */
    public void steer(Steer dir) {
        steer(Protocol.getDegreesFromSteer(dir));
    }

    /**
     * Resets both the throttle and steering direction of the car to the neutral state.
     */
    public void reset() {
        throttle(Throttle.NEUTRAL);
        steer(Steer.NEUTRAL);
    }
}
