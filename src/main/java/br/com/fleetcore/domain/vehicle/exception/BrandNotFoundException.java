package br.com.fleetcore.domain.vehicle.exception;

public class BrandNotFoundException extends RuntimeException {

    public BrandNotFoundException(String message){
        super(message);
    }
}