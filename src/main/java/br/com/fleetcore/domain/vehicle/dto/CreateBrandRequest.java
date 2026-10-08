package br.com.fleetcore.domain.vehicle.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to create a new brand")
public record CreateBrandRequest(

        @Schema(description = "Brand name", example = "Toyota", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Brand name is required")
        @Size(max = 100, message = "Brand name must have at most 100 characters")
        String name

) {}
