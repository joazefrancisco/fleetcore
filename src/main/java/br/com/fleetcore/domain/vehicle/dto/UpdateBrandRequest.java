package br.com.fleetcore.domain.vehicle.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateBrandRequest(

        @NotBlank
        @Size(max = 100)
        String name

) {}