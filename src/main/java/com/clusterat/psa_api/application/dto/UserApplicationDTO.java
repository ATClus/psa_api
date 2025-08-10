package com.clusterat.psa_api.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public final class UserApplicationDTO {
    private UserApplicationDTO() {

    }

    public static record CreateCommand(
            @NotNull(message = "Cognito ID is required")
            @Min(value = 1, message = "Cognito ID must be positive")
            Integer cognitoId
    ) {}

    @Schema(description = "User response data")
    public static record Response(
            @Schema(
                description = "Unique identifier of the user in the system",
                example = "1"
            )
            int id,
            
            @Schema(
                description = "AWS Cognito User ID associated with this user",
                example = "12345"
            )
            int cognitoId
    ) {}
}
