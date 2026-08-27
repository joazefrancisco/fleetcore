package br.com.fleetcore.domain.vehicle.dto;

public record BrandResponse(
        Long id,
        String name,
        boolean active
) {}