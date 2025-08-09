package com.clusterat.psa_api.application.handlers;

import com.clusterat.psa_api.application.commands.CreateAddressCommand;
import com.clusterat.psa_api.application.interfaces.IAddressRepository;
import com.clusterat.psa_api.application.interfaces.ICityRepository;
import com.clusterat.psa_api.domain.entities.AddressEntity;
import com.clusterat.psa_api.domain.entities.CityEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class CreateAddressCommandHandler {
    private final IAddressRepository addressRepository;
    private final ICityRepository cityRepository;

    @Autowired
    public CreateAddressCommandHandler(IAddressRepository addressRepository, ICityRepository cityRepository) {
        this.addressRepository = addressRepository;
        this.cityRepository = cityRepository;
    }

    public CompletableFuture<AddressEntity> handle(CreateAddressCommand command) {
        return cityRepository.GetByIdAsync(command.cityId())
                .thenCompose(cityOpt -> {
                    if (cityOpt.isEmpty()) {
                        throw new IllegalArgumentException("City with ID " + command.cityId() + " not found");
                    }
                    CityEntity city = cityOpt.get();
                    AddressEntity newAddress = AddressEntity.create(
                            command.street(),
                            command.number(),
                            command.complement(),
                            command.neighborhood(),
                            city
                    );
                    return addressRepository.AddAsync(newAddress);
                });
    }
}