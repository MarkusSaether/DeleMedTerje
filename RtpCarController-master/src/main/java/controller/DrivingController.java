package controller;

import model.Car;
import model.Steer;
import model.Throttle;

public enum DrivingController {
    INSTANCE;

    private Car car;

    public static DrivingController getInstance() {
        return INSTANCE;
    }

    public void registerCar(Car car) {
        this.car = car;
    }

    public void throttle(Throttle dir) {
        car.throttle(dir);
    }

    public void steer(Steer angle) {
        car.steer(angle);
    }
}
