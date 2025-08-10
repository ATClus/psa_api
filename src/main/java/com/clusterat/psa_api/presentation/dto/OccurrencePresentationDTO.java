package com.clusterat.psa_api.presentation.dto;

import com.clusterat.psa_api.domain.value_objects.Intensity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Date;

public final class OccurrencePresentationDTO {
    private OccurrencePresentationDTO() {
    }

    @Schema(description = "Occurrence creation request")
    public static record CreateRequest(
            @Schema(
                description = "Name/title of the occurrence",
                example = "Emergency Alert",
                required = true
            )
            @NotNull(message = "Name is required")
            @NotBlank(message = "Name cannot be blank")
            String name,
            
            @Schema(
                description = "Detailed description of the occurrence",
                example = "Fire in downtown area requiring immediate evacuation",
                required = true
            )
            @NotNull(message = "Description is required")
            @NotBlank(message = "Description cannot be blank")
            String description,
            
            @Schema(
                description = "Start date and time of the occurrence (ISO 8601 format)",
                example = "2024-08-10T10:00:00.000Z",
                type = "string",
                format = "date-time",
                required = true
            )
            @NotNull(message = "Date start is required")
            Date dateStart,
            
            @Schema(
                description = "End date and time of the occurrence (ISO 8601 format). Optional - can be null for ongoing occurrences",
                example = "2024-08-10T15:00:00.000Z",
                type = "string",
                format = "date-time"
            )
            Date dateEnd,
            
            @Schema(
                description = "Last update date and time (ISO 8601 format). Optional - system can auto-generate",
                example = "2024-08-10T12:30:00.000Z",
                type = "string",
                format = "date-time"
            )
            Date dateUpdate,
            
            @Schema(
                description = "Whether the occurrence is currently active/ongoing",
                example = "true"
            )
            boolean active,
            
            @Schema(
                description = "Severity level of the occurrence",
                example = "HIGH",
                allowableValues = {"LOW", "MODERATE", "HIGH", "SEVERE", "CRITICAL"},
                required = true
            )
            @NotNull(message = "Intensity is required")
            Intensity intensity,
            
            @Schema(
                description = "Unique identifier of the address where the occurrence is taking place",
                example = "1",
                minimum = "1",
                required = true
            )
            @NotNull(message = "Address ID is required")
            @Min(value = 1, message = "Address ID must be positive")
            Integer addressId,
            
            @Schema(
                description = "Unique identifier of the user creating/reporting the occurrence",
                example = "123",
                minimum = "1",
                required = true
            )
            @NotNull(message = "User ID is required")
            @Min(value = 1, message = "User ID must be positive")
            Integer userId
    ) {}

    @Schema(description = "Occurrence update request")
    public static record UpdateRequest(
            @Schema(
                description = "Updated name/title of the occurrence",
                example = "Updated Emergency Alert",
                required = true
            )
            @NotNull(message = "Name is required")
            @NotBlank(message = "Name cannot be blank")
            String name,
            
            @Schema(
                description = "Updated detailed description of the occurrence",
                example = "Fire contained in downtown area, evacuation no longer required",
                required = true
            )
            @NotNull(message = "Description is required")
            @NotBlank(message = "Description cannot be blank")
            String description,
            
            @Schema(
                description = "Updated start date and time of the occurrence (ISO 8601 format)",
                example = "2024-08-10T10:00:00.000Z",
                type = "string",
                format = "date-time",
                required = true
            )
            @NotNull(message = "Date start is required")
            Date dateStart,
            
            @Schema(
                description = "Updated end date and time of the occurrence (ISO 8601 format)",
                example = "2024-08-10T14:00:00.000Z",
                type = "string",
                format = "date-time"
            )
            Date dateEnd,
            
            @Schema(
                description = "Update timestamp (ISO 8601 format)",
                example = "2024-08-10T13:00:00.000Z",
                type = "string",
                format = "date-time"
            )
            Date dateUpdate,
            
            @Schema(
                description = "Updated active status of the occurrence",
                example = "false"
            )
            boolean active,
            
            @Schema(
                description = "Updated severity level of the occurrence",
                example = "MODERATE",
                allowableValues = {"LOW", "MODERATE", "HIGH", "SEVERE", "CRITICAL"},
                required = true
            )
            @NotNull(message = "Intensity is required")
            Intensity intensity,
            
            @Schema(
                description = "Updated address ID where the occurrence is taking place",
                example = "1",
                minimum = "1",
                required = true
            )
            @NotNull(message = "Address ID is required")
            @Min(value = 1, message = "Address ID must be positive")
            Integer addressId,
            
            @Schema(
                description = "User ID responsible for the update",
                example = "123",
                minimum = "1",
                required = true
            )
            @NotNull(message = "User ID is required")
            @Min(value = 1, message = "User ID must be positive")
            Integer userId
    ) {}
}