package br.com.fleetcore.domain.vehicle.dto;

import java.time.LocalDateTime;

public record BrandDetails(
        Long id,
        String name,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}