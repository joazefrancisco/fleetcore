package br.com.fleetcore.domain.vehicle.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Brand creation response")
public record BrandResponse(
        @Schema(description = "Brand ID", example = "1")
        Long id,
        @Schema(description = "Brand name", example = "Toyota")
        String name,
        @Schema(description = "Brand status (active/inactive)", example = "true")
        boolean active
) {}