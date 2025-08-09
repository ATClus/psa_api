package com.clusterat.psa_api.infrastructure.persistence;

import com.clusterat.psa_api.application.interfaces.IPoliceDepartmentRepository;
import com.clusterat.psa_api.domain.entities.PoliceDepartmentEntity;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Repository
public class PoliceDepartmentRepository implements IPoliceDepartmentRepository {
    private final SpringDataJpaPoliceDepartmentRepository jpaRepository;

    public PoliceDepartmentRepository(SpringDataJpaPoliceDepartmentRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public CompletableFuture<Optional<PoliceDepartmentEntity>> GetByIdAsync(int id) {
        return CompletableFuture.supplyAsync(() -> jpaRepository.findById(id));
    }

    @Override
    public CompletableFuture<Optional<PoliceDepartmentEntity>> GetByOverpassIdAsync(String overpassId) {
        return CompletableFuture.supplyAsync(() -> jpaRepository.findByOverpassId(overpassId));
    }

    @Override
    public CompletableFuture<List<Optional<PoliceDepartmentEntity>>> GetAllAsync() {
        return CompletableFuture.supplyAsync(() ->
            jpaRepository.findAll().stream()
                .map(Optional::of)
                .toList()
        );
    }

    @Override
    public CompletableFuture<PoliceDepartmentEntity> AddAsync(PoliceDepartmentEntity policeDepartment) {
        return CompletableFuture.supplyAsync(() -> jpaRepository.save(policeDepartment));
    }

    @Override
    public CompletableFuture<PoliceDepartmentEntity> UpdateAsync(PoliceDepartmentEntity policeDepartment) {
        return CompletableFuture.supplyAsync(() -> jpaRepository.save(policeDepartment));
    }

    @Override
    public CompletableFuture<PoliceDepartmentEntity> DeleteAsync(int id) {
        return CompletableFuture.supplyAsync(() -> {
            Optional<PoliceDepartmentEntity> policeDepartment = jpaRepository.findById(id);
            if (policeDepartment.isPresent()) {
                jpaRepository.deleteById(id);
                return policeDepartment.get();
            }
            throw new RuntimeException("Police Department not found with id: " + id);
        });
    }
}