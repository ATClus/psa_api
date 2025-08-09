package com.clusterat.psa_api.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public final class CityApplicationDTO {
    private CityApplicationDTO() {
    }

    public static record CreateCommand(
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
            Integer stateId
    ) {}

    public static record Response(
            int id,
            String name,
            String shortName,
            String ibgeCode,
            int stateId
    ) {}
}