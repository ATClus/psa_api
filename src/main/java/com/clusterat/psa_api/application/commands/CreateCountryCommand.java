package com.clusterat.psa_api.application.commands;

public record CreateCountryCommand(
        String name,
        String shortName,
        String isoCode
) {
}