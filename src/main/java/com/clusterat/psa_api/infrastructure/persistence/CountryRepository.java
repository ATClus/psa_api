package com.clusterat.psa_api.infrastructure.persistence;

import com.clusterat.psa_api.application.interfaces.ICountryRepository;
import com.clusterat.psa_api.domain.entities.CountryEntity;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Repository
public class CountryRepository implements ICountryRepository {
    private final SpringDataJpaCountryRepository jpaRepository;

    public CountryRepository(SpringDataJpaCountryRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public CompletableFuture<Optional<CountryEntity>> GetByIdAsync(int id) {
        return CompletableFuture.supplyAsync(() -> jpaRepository.findById(id));
    }

    @Override
    public CompletableFuture<Optional<CountryEntity>> GetByIsoCodeAsync(String isoCode) {
        return CompletableFuture.supplyAsync(() -> jpaRepository.findByIsoCode(isoCode));
    }

    @Override
    public CompletableFuture<List<Optional<CountryEntity>>> GetAllAsync() {
        return CompletableFuture.supplyAsync(() ->
            jpaRepository.findAll().stream()
                .map(Optional::of)
                .toList()
        );
    }

    @Override
    public CompletableFuture<CountryEntity> AddAsync(CountryEntity country) {
        return CompletableFuture.supplyAsync(() -> jpaRepository.save(country));
    }

    @Override
    public CompletableFuture<CountryEntity> UpdateAsync(CountryEntity country) {
        return CompletableFuture.supplyAsync(() -> jpaRepository.save(country));
    }

    @Override
    public CompletableFuture<CountryEntity> DeleteAsync(int id) {
        return CompletableFuture.supplyAsync(() -> {
            Optional<CountryEntity> country = jpaRepository.findById(id);
            if (country.isPresent()) {
                jpaRepository.deleteById(id);
                return country.get();
            }
            throw new RuntimeException("Country not found with id: " + id);
        });
    }
}