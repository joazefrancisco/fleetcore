package br.com.fleetcore.domain.vehicle.exception;

public class ModelInactiveException extends RuntimeException {

    public ModelInactiveException(String message) {
        super(message);
    }
}
