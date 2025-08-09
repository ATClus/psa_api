package com.clusterat.psa_api.infrastructure.persistence;

import com.clusterat.psa_api.application.interfaces.IAddressRepository;
import com.clusterat.psa_api.domain.entities.AddressEntity;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Repository
public class AddressRepository implements IAddressRepository {
    private final SpringDataJpaAddressRepository jpaRepository;

    public AddressRepository(SpringDataJpaAddressRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public CompletableFuture<Optional<AddressEntity>> GetByIdAsync(int id) {
        return CompletableFuture.supplyAsync(() -> jpaRepository.findById(id));
    }

    @Override
    public CompletableFuture<List<Optional<AddressEntity>>> GetAllAsync() {
        return CompletableFuture.supplyAsync(() ->
            jpaRepository.findAll().stream()
                .map(Optional::of)
                .toList()
        );
    }

    @Override
    public CompletableFuture<AddressEntity> AddAsync(AddressEntity address) {
        return CompletableFuture.supplyAsync(() -> jpaRepository.save(address));
    }

    @Override
    public CompletableFuture<AddressEntity> UpdateAsync(AddressEntity address) {
        return CompletableFuture.supplyAsync(() -> jpaRepository.save(address));
    }

    @Override
    public CompletableFuture<AddressEntity> DeleteAsync(int id) {
        return CompletableFuture.supplyAsync(() -> {
            Optional<AddressEntity> address = jpaRepository.findById(id);
            if (address.isPresent()) {
                jpaRepository.deleteById(id);
                return address.get();
            }
            throw new RuntimeException("Address not found with id: " + id);
        });
    }
}