package com.clusterat.psa_api.application.handlers;

import com.clusterat.psa_api.application.commands.CreateCountryCommand;
import com.clusterat.psa_api.application.interfaces.ICountryRepository;
import com.clusterat.psa_api.domain.entities.CountryEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class CreateCountryCommandHandler {
    private final ICountryRepository countryRepository;

    @Autowired
    public CreateCountryCommandHandler(ICountryRepository countryRepository) {
        this.countryRepository = countryRepository;
    }

    public CompletableFuture<CountryEntity> handle(CreateCountryCommand command) {
        CountryEntity newCountry = CountryEntity.create(
                command.name(),
                command.shortName(),
                command.isoCode()
        );
        return countryRepository.AddAsync(newCountry);
    }
}