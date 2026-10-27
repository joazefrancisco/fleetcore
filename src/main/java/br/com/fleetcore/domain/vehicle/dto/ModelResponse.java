package br.com.fleetcore.domain.vehicle.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Model creation response")
public record ModelResponse(
        @Schema(description = "Model ID", example = "1")
        Long id,
        @Schema(description = "Model name", example = "FH 540")
        String name,
        @Schema(description = "Brand ID", example = "1")
        Long brandId,
        @Schema(description = "Brand name", example = "Volvo")
        String brandName,
        @Schema(description = "Model status (active/inactive)", example = "true")
        boolean active
) {}
