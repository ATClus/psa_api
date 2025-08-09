package com.clusterat.psa_api.application.interfaces;

import com.clusterat.psa_api.domain.entities.StateEntity;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface IStateRepository {
    CompletableFuture<Optional<StateEntity>> GetByIdAsync(int id);
    CompletableFuture<Optional<StateEntity>> GetByIbgeCodeAsync(String ibgeCode);
    CompletableFuture<List<Optional<StateEntity>>> GetAllAsync();
    CompletableFuture<StateEntity> AddAsync(StateEntity state);
    CompletableFuture<StateEntity> UpdateAsync(StateEntity state);
    CompletableFuture<StateEntity> DeleteAsync(int id);
}
