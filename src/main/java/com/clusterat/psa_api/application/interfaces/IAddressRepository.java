package com.clusterat.psa_api.application.interfaces;

import com.clusterat.psa_api.domain.entities.AddressEntity;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface IAddressRepository {
    CompletableFuture<Optional<AddressEntity>> GetByIdAsync(int id);
    CompletableFuture<List<Optional<AddressEntity>>> GetAllAsync();
    CompletableFuture<AddressEntity> AddAsync(AddressEntity address);
    CompletableFuture<AddressEntity> UpdateAsync(AddressEntity address);
    CompletableFuture<AddressEntity> DeleteAsync(int id);
}
