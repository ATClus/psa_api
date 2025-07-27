package com.clusterat.psa_api.presentation.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public final class UserPresentationDTO {
    private UserPresentationDTO() {

    }

    public static record CreateRequest(
            @NotNull(message = "Cognito ID is required")
            @Min(value = 1, message = "Cognito ID must be positive")
            Integer cognitoId
    ) {}
}
