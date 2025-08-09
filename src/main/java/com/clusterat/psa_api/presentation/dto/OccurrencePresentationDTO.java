package com.clusterat.psa_api.presentation.dto;

import com.clusterat.psa_api.domain.value_objects.Intensity;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Date;

public final class OccurrencePresentationDTO {
    private OccurrencePresentationDTO() {
    }

    public static record CreateRequest(
            @NotNull(message = "Name is required")
            @NotBlank(message = "Name cannot be blank")
            String name,
            
            @NotNull(message = "Description is required")
            @NotBlank(message = "Description cannot be blank")
            String description,
            
            @NotNull(message = "Date start is required")
            Date dateStart,
            
            Date dateEnd,
            
            Date dateUpdate,
            
            boolean active,
            
            @NotNull(message = "Intensity is required")
            Intensity intensity,
            
            @NotNull(message = "Address ID is required")
            @Min(value = 1, message = "Address ID must be positive")
            Integer addressId,
            
            @NotNull(message = "User ID is required")
            @Min(value = 1, message = "User ID must be positive")
            Integer userId
    ) {}

    public static record UpdateRequest(
            @NotNull(message = "Name is required")
            @NotBlank(message = "Name cannot be blank")
            String name,
            
            @NotNull(message = "Description is required")
            @NotBlank(message = "Description cannot be blank")
            String description,
            
            @NotNull(message = "Date start is required")
            Date dateStart,
            
            Date dateEnd,
            
            Date dateUpdate,
            
            boolean active,
            
            @NotNull(message = "Intensity is required")
            Intensity intensity,
            
            @NotNull(message = "Address ID is required")
            @Min(value = 1, message = "Address ID must be positive")
            Integer addressId,
            
            @NotNull(message = "User ID is required")
            @Min(value = 1, message = "User ID must be positive")
            Integer userId
    ) {}
}