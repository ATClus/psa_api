package com.clusterat.psa_api.application.dto;

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

    public static record Response(
            int id,
            int cognitoId
    ) {}
}
