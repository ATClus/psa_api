package com.clusterat.psa_api.application.commands;

public record CreatePoliceDepartmentCommand(
        String overpassId,
        String name,
        String shortName,
        String operator,
        String ownership,
        String phone,
        String email,
        String latitude,
        String longitude,
        int addressId
) {
}