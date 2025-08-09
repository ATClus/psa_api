package com.clusterat.psa_api.presentation.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public final class AddressPresentationDTO {
    private AddressPresentationDTO() {
    }

    public static record CreateRequest(
            @NotNull(message = "Street is required")
            @NotBlank(message = "Street cannot be blank")
            String street,
            
            @NotNull(message = "Number is required")
            @NotBlank(message = "Number cannot be blank")
            String number,
            
            @NotNull(message = "Complement is required")
            @NotBlank(message = "Complement cannot be blank")
            String complement,
            
            @NotNull(message = "Neighborhood is required")
            @NotBlank(message = "Neighborhood cannot be blank")
            String neighborhood,
            
            @NotNull(message = "City ID is required")
            @Min(value = 1, message = "City ID must be positive")
            Integer cityId
    ) {}

    public static record UpdateRequest(
            @NotNull(message = "Street is required")
            @NotBlank(message = "Street cannot be blank")
            String street,
            
            @NotNull(message = "Number is required")
            @NotBlank(message = "Number cannot be blank")
            String number,
            
            @NotNull(message = "Complement is required")
            @NotBlank(message = "Complement cannot be blank")
            String complement,
            
            @NotNull(message = "Neighborhood is required")
            @NotBlank(message = "Neighborhood cannot be blank")
            String neighborhood,
            
            @NotNull(message = "City ID is required")
            @Min(value = 1, message = "City ID must be positive")
            Integer cityId
    ) {}
}