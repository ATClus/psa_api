package com.clusterat.psa_api.application.interfaces;

import com.clusterat.psa_api.domain.entities.CityEntity;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface ICityRepository {
    CompletableFuture<Optional<CityEntity>> GetByIdAsync(int id);
    CompletableFuture<Optional<CityEntity>> GetByIbgeCodeAsync(String ibgeCode);
    CompletableFuture<List<Optional<CityEntity>>> GetAllAsync();
    CompletableFuture<CityEntity> AddAsync(CityEntity city);
    CompletableFuture<CityEntity> UpdateAsync(CityEntity city);
    CompletableFuture<CityEntity> DeleteAsync(int id);
}
