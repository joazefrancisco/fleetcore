package br.com.fleetcore.domain.vehicle.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to create a new model")
public record CreateModelRequest(

        @Schema(description = "Model name", example = "FH 540", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Model name is required")
        @Size(max = 100, message = "Model name must have at most 100 characters")
        String name,

        @Schema(description = "Brand ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Brand ID is required")
        Long brandId

) {}
