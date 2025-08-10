package com.clusterat.psa_api.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public final class UserPresentationDTO {
    private UserPresentationDTO() {

    }

    @Schema(description = "User creation request")
    public static record CreateRequest(
            @Schema(
                description = "AWS Cognito User ID - unique identifier from Cognito user pool",
                example = "12345",
                minimum = "1",
                required = true
            )
            @NotNull(message = "Cognito ID is required")
            @Min(value = 1, message = "Cognito ID must be positive")
            Integer cognitoId
    ) {}
}
