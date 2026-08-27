package br.com.fleetcore.domain.vehicle.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateBrandRequest(

        @NotBlank(message = "Brand name is required")
        @Size(max = 100, message = "Brand name must have at most 100 characters")
        String name

) {}
