package br.com.fleetcore.domain.vehicle.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "Complete brand details")
public record BrandDetails(
        @Schema(description = "Brand ID", example = "1")
        Long id,
        @Schema(description = "Brand name", example = "Toyota")
        String name,
        @Schema(description = "Brand status (active/inactive)", example = "true")
        boolean active,
        @Schema(description = "Creation date", example = "2024-01-15T10:30:00")
        LocalDateTime createdAt,
        @Schema(description = "Last update date", example = "2024-01-20T14:45:00")
        LocalDateTime updatedAt
) {}