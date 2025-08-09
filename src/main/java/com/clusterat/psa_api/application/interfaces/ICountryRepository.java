package com.clusterat.psa_api.application.interfaces;

import com.clusterat.psa_api.domain.entities.CountryEntity;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface ICountryRepository {
    CompletableFuture<Optional<CountryEntity>> GetByIdAsync(int id);
    CompletableFuture<Optional<CountryEntity>> GetByIsoCodeAsync(String isoCode);
    CompletableFuture<List<Optional<CountryEntity>>> GetAllAsync();
    CompletableFuture<CountryEntity> AddAsync(CountryEntity country);
    CompletableFuture<CountryEntity> UpdateAsync(CountryEntity country);
    CompletableFuture<CountryEntity> DeleteAsync(int id);
}
