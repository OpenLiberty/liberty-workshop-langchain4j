package com.carmanagement.exceptions;

public class CarInfoNotFoundException extends RuntimeException {
    public CarInfoNotFoundException(int carInfoId) {
        super("CarInfo %d not found".formatted(carInfoId));
    }
}
