package com.clusterat.psa_api.infrastructure.persistence;

import com.clusterat.psa_api.application.interfaces.IOccurrenceRepository;
import com.clusterat.psa_api.domain.entities.OccurrenceEntity;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Repository
public class OccurrenceRepository implements IOccurrenceRepository {
    private final SpringDataJpaOccurrenceRepository jpaRepository;

    public OccurrenceRepository(SpringDataJpaOccurrenceRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public CompletableFuture<Optional<OccurrenceEntity>> GetByIdAsync(int id) {
        return CompletableFuture.supplyAsync(() -> jpaRepository.findById(id));
    }

    @Override
    public CompletableFuture<List<Optional<OccurrenceEntity>>> GetAllAsync() {
        return CompletableFuture.supplyAsync(() ->
            jpaRepository.findAll().stream()
                .map(Optional::of)
                .toList()
        );
    }

    @Override
    public CompletableFuture<List<Optional<OccurrenceEntity>>> GetByActiveAsync(boolean active) {
        return CompletableFuture.supplyAsync(() ->
            jpaRepository.findByActive(active).stream()
                .map(Optional::of)
                .toList()
        );
    }

    @Override
    public CompletableFuture<List<Optional<OccurrenceEntity>>> GetByUserIdAsync(int userId) {
        return CompletableFuture.supplyAsync(() ->
            jpaRepository.findByUserId(userId).stream()
                .map(Optional::of)
                .toList()
        );
    }

    @Override
    public CompletableFuture<OccurrenceEntity> AddAsync(OccurrenceEntity occurrence) {
        return CompletableFuture.supplyAsync(() -> jpaRepository.save(occurrence));
    }

    @Override
    public CompletableFuture<OccurrenceEntity> UpdateAsync(OccurrenceEntity occurrence) {
        return CompletableFuture.supplyAsync(() -> jpaRepository.save(occurrence));
    }

    @Override
    public CompletableFuture<OccurrenceEntity> DeleteAsync(int id) {
        return CompletableFuture.supplyAsync(() -> {
            Optional<OccurrenceEntity> occurrence = jpaRepository.findById(id);
            if (occurrence.isPresent()) {
                jpaRepository.deleteById(id);
                return occurrence.get();
            }
            throw new RuntimeException("Occurrence not found with id: " + id);
        });
    }
}