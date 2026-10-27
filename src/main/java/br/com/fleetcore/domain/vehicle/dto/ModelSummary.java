package br.com.fleetcore.domain.vehicle.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Model summary for listing")
public record ModelSummary(
        @Schema(description = "Model ID", example = "1")
        Long id,
        @Schema(description = "Model name", example = "FH 540")
        String name,
        @Schema(description = "Brand ID", example = "1")
        Long brandId,
        @Schema(description = "Brand name", example = "Volvo")
        String brandName
) {}
