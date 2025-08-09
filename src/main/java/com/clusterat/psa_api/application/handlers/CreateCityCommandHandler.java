package com.clusterat.psa_api.application.handlers;

import com.clusterat.psa_api.application.commands.CreateCityCommand;
import com.clusterat.psa_api.application.interfaces.ICityRepository;
import com.clusterat.psa_api.application.interfaces.IStateRepository;
import com.clusterat.psa_api.domain.entities.CityEntity;
import com.clusterat.psa_api.domain.entities.StateEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class CreateCityCommandHandler {
    private final ICityRepository cityRepository;
    private final IStateRepository stateRepository;

    @Autowired
    public CreateCityCommandHandler(ICityRepository cityRepository, IStateRepository stateRepository) {
        this.cityRepository = cityRepository;
        this.stateRepository = stateRepository;
    }

    public CompletableFuture<CityEntity> handle(CreateCityCommand command) {
        return stateRepository.GetByIdAsync(command.stateId())
                .thenCompose(stateOpt -> {
                    if (stateOpt.isEmpty()) {
                        throw new IllegalArgumentException("State with ID " + command.stateId() + " not found");
                    }
                    StateEntity state = stateOpt.get();
                    CityEntity newCity = CityEntity.create(
                            command.name(),
                            command.shortName(),
                            command.ibgeCode(),
                            state
                    );
                    return cityRepository.AddAsync(newCity);
                });
    }
}