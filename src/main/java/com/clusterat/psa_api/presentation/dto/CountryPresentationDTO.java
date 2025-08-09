package com.clusterat.psa_api.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public final class CountryPresentationDTO {
    private CountryPresentationDTO() {
    }

    public static record CreateRequest(
            @NotNull(message = "Name is required")
            @NotBlank(message = "Name cannot be blank")
            String name,
            
            @NotNull(message = "Short name is required")
            @NotBlank(message = "Short name cannot be blank")
            String shortName,
            
            @NotNull(message = "ISO code is required")
            @NotBlank(message = "ISO code cannot be blank")
            String isoCode
    ) {}

    public static record UpdateRequest(
            @NotNull(message = "Name is required")
            @NotBlank(message = "Name cannot be blank")
            String name,
            
            @NotNull(message = "Short name is required")
            @NotBlank(message = "Short name cannot be blank")
            String shortName,
            
            @NotNull(message = "ISO code is required")
            @NotBlank(message = "ISO code cannot be blank")
            String isoCode
    ) {}
}