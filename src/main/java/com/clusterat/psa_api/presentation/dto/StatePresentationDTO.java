package com.clusterat.psa_api.presentation.dto;

import com.clusterat.psa_api.domain.value_objects.Region;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public final class StatePresentationDTO {
    private StatePresentationDTO() {
    }

    @Schema(description = "State creation request")
    public static record CreateRequest(
            @Schema(
                description = "Full name of the state",
                example = "São Paulo",
                required = true
            )
            @NotNull(message = "Name is required")
            @NotBlank(message = "Name cannot be blank")
            String name,
            
            @Schema(
                description = "Short name or abbreviation of the state",
                example = "SP",
                maxLength = 5,
                required = true
            )
            @NotNull(message = "Short name is required")
            @NotBlank(message = "Short name cannot be blank")
            String shortName,
            
            @Schema(
                description = "Geographic region of the state within the country",
                example = "SUDESTE",
                allowableValues = {"NORTE", "NORDESTE", "CENTRO_OESTE", "SUDESTE", "SUL"},
                required = true
            )
            @NotNull(message = "Region is required")
            Region region,
            
            @Schema(
                description = "Brazilian IBGE (Instituto Brasileiro de Geografia e Estatística) code for the state",
                example = "35",
                pattern = "^[0-9]{1,2}$",
                required = true
            )
            @NotNull(message = "IBGE code is required")
            @NotBlank(message = "IBGE code cannot be blank")
            String ibgeCode,
            
            @Schema(
                description = "Unique identifier of the country this state belongs to",
                example = "1",
                minimum = "1",
                required = true
            )
            @NotNull(message = "Country ID is required")
            @Min(value = 1, message = "Country ID must be positive")
            Integer countryId
    ) {}

    @Schema(description = "State update request")
    public static record UpdateRequest(
            @Schema(
                description = "Updated full name of the state",
                example = "São Paulo",
                required = true
            )
            @NotNull(message = "Name is required")
            @NotBlank(message = "Name cannot be blank")
            String name,
            
            @Schema(
                description = "Updated short name or abbreviation of the state",
                example = "SP",
                maxLength = 5,
                required = true
            )
            @NotNull(message = "Short name is required")
            @NotBlank(message = "Short name cannot be blank")
            String shortName,
            
            @Schema(
                description = "Updated geographic region of the state",
                example = "SUDESTE",
                allowableValues = {"NORTE", "NORDESTE", "CENTRO_OESTE", "SUDESTE", "SUL"},
                required = true
            )
            @NotNull(message = "Region is required")
            Region region,
            
            @Schema(
                description = "Updated IBGE code for the state",
                example = "35",
                pattern = "^[0-9]{1,2}$",
                required = true
            )
            @NotNull(message = "IBGE code is required")
            @NotBlank(message = "IBGE code cannot be blank")
            String ibgeCode,
            
            @Schema(
                description = "Updated country ID this state belongs to",
                example = "1",
                minimum = "1",
                required = true
            )
            @NotNull(message = "Country ID is required")
            @Min(value = 1, message = "Country ID must be positive")
            Integer countryId
    ) {}
}