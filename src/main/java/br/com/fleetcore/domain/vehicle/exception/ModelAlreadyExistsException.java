package br.com.fleetcore.domain.vehicle.exception;

public class ModelAlreadyExistsException extends RuntimeException {

    public ModelAlreadyExistsException(String message) {
        super(message);
    }
}
