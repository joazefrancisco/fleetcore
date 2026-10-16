package br.com.fleetcore.domain.vehicle.exception;

public class BrandAlreadyExistsException extends RuntimeException {

    public BrandAlreadyExistsException(String message){
        super(message);
    }
}