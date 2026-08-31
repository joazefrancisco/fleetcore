package br.com.fleetcore.exception;

public record FieldErrorResponse(
        String field,
        String name
) {}
