package br.com.fleetcore.domain.vehicle.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Model status filter for listing")
public enum ModelStatusFilter {
    @Schema(description = "Only active models")
    ACTIVE,
    @Schema(description = "Only inactive models")
    INACTIVE,
    @Schema(description = "All models, regardless of status")
    ALL
}
