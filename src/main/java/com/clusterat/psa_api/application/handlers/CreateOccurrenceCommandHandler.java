package com.clusterat.psa_api.application.handlers;

import com.clusterat.psa_api.application.commands.CreateOccurrenceCommand;
import com.clusterat.psa_api.application.interfaces.IAddressRepository;
import com.clusterat.psa_api.application.interfaces.IOccurrenceRepository;
import com.clusterat.psa_api.application.interfaces.IUserRepository;
import com.clusterat.psa_api.domain.entities.AddressEntity;
import com.clusterat.psa_api.domain.entities.OccurrenceEntity;
import com.clusterat.psa_api.domain.entities.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class CreateOccurrenceCommandHandler {
    private final IOccurrenceRepository occurrenceRepository;
    private final IAddressRepository addressRepository;
    private final IUserRepository userRepository;

    @Autowired
    public CreateOccurrenceCommandHandler(IOccurrenceRepository occurrenceRepository, IAddressRepository addressRepository, IUserRepository userRepository) {
        this.occurrenceRepository = occurrenceRepository;
        this.addressRepository = addressRepository;
        this.userRepository = userRepository;
    }

    public CompletableFuture<OccurrenceEntity> handle(CreateOccurrenceCommand command) {
        CompletableFuture<AddressEntity> addressFuture = addressRepository.GetByIdAsync(command.addressId())
                .thenApply(addressOpt -> {
                    if (addressOpt.isEmpty()) {
                        throw new IllegalArgumentException("Address with ID " + command.addressId() + " not found");
                    }
                    return addressOpt.get();
                });

        CompletableFuture<UserEntity> userFuture = userRepository.GetByIdAsync(command.userId())
                .thenApply(userOpt -> {
                    if (userOpt.isEmpty()) {
                        throw new IllegalArgumentException("User with ID " + command.userId() + " not found");
                    }
                    return userOpt.get();
                });

        return CompletableFuture.allOf(addressFuture, userFuture)
                .thenCompose(v -> {
                    AddressEntity address = addressFuture.join();
                    UserEntity user = userFuture.join();
                    
                    OccurrenceEntity newOccurrence = OccurrenceEntity.create(
                            command.name(),
                            command.description(),
                            command.dateStart(),
                            command.dateEnd(),
                            command.dateUpdate(),
                            command.active(),
                            command.intensity(),
                            address,
                            user
                    );
                    return occurrenceRepository.AddAsync(newOccurrence);
                });
    }
}