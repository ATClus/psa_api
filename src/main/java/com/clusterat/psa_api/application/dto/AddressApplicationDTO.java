package com.clusterat.psa_api.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public final class AddressApplicationDTO {
    private AddressApplicationDTO() {
    }

    public static record CreateCommand(
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
            Integer cityId
    ) {}

    public static record Response(
            int id,
            String street,
            String number,
            String complement,
            String neighborhood,
            int cityId
    ) {}
}