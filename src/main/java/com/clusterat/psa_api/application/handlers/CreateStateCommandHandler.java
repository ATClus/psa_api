package com.clusterat.psa_api.application.handlers;

import com.clusterat.psa_api.application.commands.CreateStateCommand;
import com.clusterat.psa_api.application.interfaces.ICountryRepository;
import com.clusterat.psa_api.application.interfaces.IStateRepository;
import com.clusterat.psa_api.domain.entities.CountryEntity;
import com.clusterat.psa_api.domain.entities.StateEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class CreateStateCommandHandler {
    private final IStateRepository stateRepository;
    private final ICountryRepository countryRepository;

    @Autowired
    public CreateStateCommandHandler(IStateRepository stateRepository, ICountryRepository countryRepository) {
        this.stateRepository = stateRepository;
        this.countryRepository = countryRepository;
    }

    public CompletableFuture<StateEntity> handle(CreateStateCommand command) {
        return countryRepository.GetByIdAsync(command.countryId())
                .thenCompose(countryOpt -> {
                    if (countryOpt.isEmpty()) {
                        throw new IllegalArgumentException("Country with ID " + command.countryId() + " not found");
                    }
                    CountryEntity country = countryOpt.get();
                    StateEntity newState = StateEntity.create(
                            command.name(),
                            command.shortName(),
                            command.region(),
                            command.ibgeCode(),
                            country
                    );
                    return stateRepository.AddAsync(newState);
                });
    }
}