package com.clusterat.psa_api.application.interfaces;

import com.clusterat.psa_api.domain.entities.OccurrenceEntity;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface IOccurrenceRepository {
    CompletableFuture<Optional<OccurrenceEntity>> GetByIdAsync(int id);
    CompletableFuture<List<Optional<OccurrenceEntity>>> GetAllAsync();
    CompletableFuture<List<Optional<OccurrenceEntity>>> GetByActiveAsync(boolean active);
    CompletableFuture<List<Optional<OccurrenceEntity>>> GetByUserIdAsync(int userId);
    CompletableFuture<OccurrenceEntity> AddAsync(OccurrenceEntity occurrence);
    CompletableFuture<OccurrenceEntity> UpdateAsync(OccurrenceEntity occurrence);
    CompletableFuture<OccurrenceEntity> DeleteAsync(int id);
}
