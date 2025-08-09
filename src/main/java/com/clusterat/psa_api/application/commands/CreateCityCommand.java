package com.clusterat.psa_api.application.commands;

public record CreateCityCommand(
        String name,
        String shortName,
        String ibgeCode,
        int stateId
) {
}