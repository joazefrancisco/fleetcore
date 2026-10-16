package br.com.fleetcore.domain.vehicle.exception;

public class BrandInactiveException extends RuntimeException {

    public BrandInactiveException(String message){
        super(message);
    }
}
