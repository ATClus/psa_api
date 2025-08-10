package com.clusterat.psa_api.application.dto;

import com.clusterat.psa_api.domain.value_objects.Intensity;
import io.swagger.v3.oas.annotations.media.Schema;
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

    @Schema(description = "Occurrence response data")
    public static record Response(
            @Schema(
                description = "Unique identifier of the occurrence",
                example = "1"
            )
            int id,
            
            @Schema(
                description = "Name/title of the occurrence",
                example = "Emergency Alert"
            )
            String name,
            
            @Schema(
                description = "Detailed description of the occurrence",
                example = "Fire in downtown area requiring immediate evacuation"
            )
            String description,
            
            @Schema(
                description = "Start date and time of the occurrence",
                example = "2024-08-10T10:00:00.000Z",
                type = "string",
                format = "date-time"
            )
            Date dateStart,
            
            @Schema(
                description = "End date and time of the occurrence (null if ongoing)",
                example = "2024-08-10T15:00:00.000Z",
                type = "string",
                format = "date-time",
                nullable = true
            )
            Date dateEnd,
            
            @Schema(
                description = "Last update timestamp",
                example = "2024-08-10T12:30:00.000Z",
                type = "string",
                format = "date-time",
                nullable = true
            )
            Date dateUpdate,
            
            @Schema(
                description = "Whether the occurrence is currently active",
                example = "true"
            )
            boolean active,
            
            @Schema(
                description = "Severity level of the occurrence",
                example = "HIGH",
                allowableValues = {"LOW", "MODERATE", "HIGH", "SEVERE", "CRITICAL"}
            )
            Intensity intensity,
            
            @Schema(
                description = "ID of the address where the occurrence is taking place",
                example = "1"
            )
            int addressId,
            
            @Schema(
                description = "ID of the user who created/reported the occurrence",
                example = "123"
            )
            int userId
    ) {}
}