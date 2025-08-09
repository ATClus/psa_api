package com.clusterat.psa_api.infrastructure.persistence;

import com.clusterat.psa_api.application.interfaces.ICityRepository;
import com.clusterat.psa_api.domain.entities.CityEntity;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Repository
public class CityRepository implements ICityRepository {
    private final SpringDataJpaCityRepository jpaRepository;

    public CityRepository(SpringDataJpaCityRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public CompletableFuture<Optional<CityEntity>> GetByIdAsync(int id) {
        return CompletableFuture.supplyAsync(() -> jpaRepository.findById(id));
    }

    @Override
    public CompletableFuture<Optional<CityEntity>> GetByIbgeCodeAsync(String ibgeCode) {
        return CompletableFuture.supplyAsync(() -> jpaRepository.findByIbgeCode(ibgeCode));
    }

    @Override
    public CompletableFuture<List<Optional<CityEntity>>> GetAllAsync() {
        return CompletableFuture.supplyAsync(() ->
            jpaRepository.findAll().stream()
                .map(Optional::of)
                .toList()
        );
    }

    @Override
    public CompletableFuture<CityEntity> AddAsync(CityEntity city) {
        return CompletableFuture.supplyAsync(() -> jpaRepository.save(city));
    }

    @Override
    public CompletableFuture<CityEntity> UpdateAsync(CityEntity city) {
        return CompletableFuture.supplyAsync(() -> jpaRepository.save(city));
    }

    @Override
    public CompletableFuture<CityEntity> DeleteAsync(int id) {
        return CompletableFuture.supplyAsync(() -> {
            Optional<CityEntity> city = jpaRepository.findById(id);
            if (city.isPresent()) {
                jpaRepository.deleteById(id);
                return city.get();
            }
            throw new RuntimeException("City not found with id: " + id);
        });
    }
}