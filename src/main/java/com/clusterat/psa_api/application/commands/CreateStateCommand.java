package com.clusterat.psa_api.application.commands;

import com.clusterat.psa_api.domain.value_objects.Region;

public record CreateStateCommand(
        String name,
        String shortName,
        Region region,
        String ibgeCode,
        int countryId
) {
}