package com.clusterat.psa_api.application.commands;

public record CreateAddressCommand(
        String street,
        String number,
        String complement,
        String neighborhood,
        int cityId
) {
}