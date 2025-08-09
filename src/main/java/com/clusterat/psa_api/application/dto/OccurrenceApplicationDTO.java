package com.clusterat.psa_api.application.dto;

import com.clusterat.psa_api.domain.value_objects.Intensity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Date;

public final class OccurrenceApplicationDTO {
    private OccurrenceApplicationDTO() {
    }

    public static record CreateCommand(
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
            Integer addressId,
            
            @NotNull(message = "User ID is required")
            Integer userId
    ) {}

    public static record Response(
            int id,
            String name,
            String description,
            Date dateStart,
            Date dateEnd,
            Date dateUpdate,
            boolean active,
            Intensity intensity,
            int addressId,
            int userId
    ) {}
}