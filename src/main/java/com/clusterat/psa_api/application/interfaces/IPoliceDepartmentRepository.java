package com.clusterat.psa_api.application.interfaces;

import com.clusterat.psa_api.domain.entities.PoliceDepartmentEntity;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface IPoliceDepartmentRepository {
    CompletableFuture<Optional<PoliceDepartmentEntity>> GetByIdAsync(int id);
    CompletableFuture<Optional<PoliceDepartmentEntity>> GetByOverpassIdAsync(String overpassId);
    CompletableFuture<List<Optional<PoliceDepartmentEntity>>> GetAllAsync();
    CompletableFuture<PoliceDepartmentEntity> AddAsync(PoliceDepartmentEntity policeDepartment);
    CompletableFuture<PoliceDepartmentEntity> UpdateAsync(PoliceDepartmentEntity policeDepartment);
    CompletableFuture<PoliceDepartmentEntity> DeleteAsync(int id);
}
