package com.clusterat.psa_api.application.handlers;

import com.clusterat.psa_api.application.commands.CreatePoliceDepartmentCommand;
import com.clusterat.psa_api.application.interfaces.IAddressRepository;
import com.clusterat.psa_api.application.interfaces.IPoliceDepartmentRepository;
import com.clusterat.psa_api.domain.entities.AddressEntity;
import com.clusterat.psa_api.domain.entities.PoliceDepartmentEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class CreatePoliceDepartmentCommandHandler {
    private final IPoliceDepartmentRepository policeDepartmentRepository;
    private final IAddressRepository addressRepository;

    @Autowired
    public CreatePoliceDepartmentCommandHandler(IPoliceDepartmentRepository policeDepartmentRepository, IAddressRepository addressRepository) {
        this.policeDepartmentRepository = policeDepartmentRepository;
        this.addressRepository = addressRepository;
    }

    public CompletableFuture<PoliceDepartmentEntity> handle(CreatePoliceDepartmentCommand command) {
        return addressRepository.GetByIdAsync(command.addressId())
                .thenCompose(addressOpt -> {
                    if (addressOpt.isEmpty()) {
                        throw new IllegalArgumentException("Address with ID " + command.addressId() + " not found");
                    }
                    AddressEntity address = addressOpt.get();
                    PoliceDepartmentEntity newPoliceDepartment = PoliceDepartmentEntity.create(
                            command.overpassId(),
                            command.name(),
                            command.shortName(),
                            command.operator(),
                            command.ownership(),
                            command.phone(),
                            command.email(),
                            command.latitude(),
                            command.longitude(),
                            address
                    );
                    return policeDepartmentRepository.AddAsync(newPoliceDepartment);
                });
    }
}