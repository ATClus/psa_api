package com.clusterat.psa_api.presentation.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public final class CityPresentationDTO {
    private CityPresentationDTO() {
    }

    public static record CreateRequest(
            @NotNull(message = "Name is required")
            @NotBlank(message = "Name cannot be blank")
            String name,
            
            @NotNull(message = "Short name is required")
            @NotBlank(message = "Short name cannot be blank")
            String shortName,
            
            @NotNull(message = "IBGE code is required")
            @NotBlank(message = "IBGE code cannot be blank")
            String ibgeCode,
            
            @NotNull(message = "State ID is required")
            @Min(value = 1, message = "State ID must be positive")
            Integer stateId
    ) {}

    public static record UpdateRequest(
            @NotNull(message = "Name is required")
            @NotBlank(message = "Name cannot be blank")
            String name,
            
            @NotNull(message = "Short name is required")
            @NotBlank(message = "Short name cannot be blank")
            String shortName,
            
            @NotNull(message = "IBGE code is required")
            @NotBlank(message = "IBGE code cannot be blank")
            String ibgeCode,
            
            @NotNull(message = "State ID is required")
            @Min(value = 1, message = "State ID must be positive")
            Integer stateId
    ) {}
}