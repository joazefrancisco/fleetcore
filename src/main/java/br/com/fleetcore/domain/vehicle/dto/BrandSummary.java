package br.com.fleetcore.domain.vehicle.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Brand summary for listing")
public record BrandSummary(
        @Schema(description = "Brand ID", example = "1")
        Long id,
        @Schema(description = "Brand name", example = "Toyota")
        String name
) {}
