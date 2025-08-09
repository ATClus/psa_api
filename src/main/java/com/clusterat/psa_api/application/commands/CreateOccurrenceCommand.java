package com.clusterat.psa_api.application.commands;

import com.clusterat.psa_api.domain.value_objects.Intensity;

import java.util.Date;

public record CreateOccurrenceCommand(
        String name,
        String description,
        Date dateStart,
        Date dateEnd,
        Date dateUpdate,
        boolean active,
        Intensity intensity,
        int addressId,
        int userId
) {
}