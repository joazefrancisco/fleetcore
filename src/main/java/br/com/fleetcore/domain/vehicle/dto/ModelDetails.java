package br.com.fleetcore.domain.vehicle.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Complete model details")
public record ModelDetails(
        @Schema(description = "Model ID", example = "1")
        Long id,
        @Schema(description = "Model name", example = "FH 540")
        String name,
        @Schema(description = "Brand ID", example = "1")
        Long brandId,
        @Schema(description = "Brand name", example = "Volvo")
        String brandName,
        @Schema(description = "Model status (active/inactive)", example = "true")
        boolean active,
        @Schema(description = "Creation date", example = "2024-01-15T10:30:00")
        LocalDateTime createdAt,
        @Schema(description = "Last update date", example = "2024-01-20T14:45:00")
        LocalDateTime updatedAt
) {}
