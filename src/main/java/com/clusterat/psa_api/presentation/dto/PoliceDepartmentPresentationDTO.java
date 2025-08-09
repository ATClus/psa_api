package com.clusterat.psa_api.presentation.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public final class PoliceDepartmentPresentationDTO {
    private PoliceDepartmentPresentationDTO() {
    }

    public static record CreateRequest(
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
            @Email(message = "Email must be valid")
            String email,
            
            @NotNull(message = "Latitude is required")
            @NotBlank(message = "Latitude cannot be blank")
            String latitude,
            
            @NotNull(message = "Longitude is required")
            @NotBlank(message = "Longitude cannot be blank")
            String longitude,
            
            @NotNull(message = "Address ID is required")
            @Min(value = 1, message = "Address ID must be positive")
            Integer addressId
    ) {}

    public static record UpdateRequest(
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
            @Email(message = "Email must be valid")
            String email,
            
            @NotNull(message = "Latitude is required")
            @NotBlank(message = "Latitude cannot be blank")
            String latitude,
            
            @NotNull(message = "Longitude is required")
            @NotBlank(message = "Longitude cannot be blank")
            String longitude,
            
            @NotNull(message = "Address ID is required")
            @Min(value = 1, message = "Address ID must be positive")
            Integer addressId
    ) {}
}