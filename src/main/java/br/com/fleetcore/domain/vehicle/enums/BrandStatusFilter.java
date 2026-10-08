package br.com.fleetcore.domain.vehicle.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Brand status filter for listing")
public enum BrandStatusFilter {
    @Schema(description = "Only active brands")
    ACTIVE,
    @Schema(description = "Only inactive brands")
    INACTIVE,
    @Schema(description = "All brands, regardless of status")
    ALL
}
