package com.clusterat.psa_api.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public final class PoliceDepartmentApplicationDTO {
    private PoliceDepartmentApplicationDTO() {
    }

    public static record CreateCommand(
            @NotNull(message = "Overpass ID is required")
            @NotBlank(message = "Overpass ID cannot be blank")
            String overpassId,
            
            @NotNull(message = "Name is required")
            @NotBlank(message = "Name cannot be blank")
            String name,
            
            @NotNull(message = "Short name is required")
            @NotBlank(message = "Short name cannot be blank")
            String shortName,
            
            @NotNull(message = "Operator is required")
            @NotBlank(message = "Operator cannot be blank")
            String operator,
            
            @NotNull(message = "Ownership is required")
            @NotBlank(message = "Ownership cannot be blank")
            String ownership,
            
            @NotNull(message = "Phone is required")
            @NotBlank(message = "Phone cannot be blank")
            String phone,
            
            @NotNull(message = "Email is required")
            @NotBlank(message = "Email cannot be blank")
            String email,
            
            @NotNull(message = "Latitude is required")
            @NotBlank(message = "Latitude cannot be blank")
            String latitude,
            
            @NotNull(message = "Longitude is required")
            @NotBlank(message = "Longitude cannot be blank")
            String longitude,
            
            @NotNull(message = "Address ID is required")
            Integer addressId
    ) {}

    public static record Response(
            int id,
            String overpassId,
            String name,
            String shortName,
            String operator,
            String ownership,
            String phone,
            String email,
            String latitude,
            String longitude,
            int addressId
    ) {}
}