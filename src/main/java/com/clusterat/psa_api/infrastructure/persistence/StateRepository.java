package com.clusterat.psa_api.infrastructure.persistence;

import com.clusterat.psa_api.application.interfaces.IStateRepository;
import com.clusterat.psa_api.domain.entities.StateEntity;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Repository
public class StateRepository implements IStateRepository {
    private final SpringDataJpaStateRepository jpaRepository;

    public StateRepository(SpringDataJpaStateRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public CompletableFuture<Optional<StateEntity>> GetByIdAsync(int id) {
        return CompletableFuture.supplyAsync(() -> jpaRepository.findById(id));
    }

    @Override
    public CompletableFuture<Optional<StateEntity>> GetByIbgeCodeAsync(String ibgeCode) {
        return CompletableFuture.supplyAsync(() -> jpaRepository.findByIbgeCode(ibgeCode));
    }

    @Override
    public CompletableFuture<List<Optional<StateEntity>>> GetAllAsync() {
        return CompletableFuture.supplyAsync(() ->
            jpaRepository.findAll().stream()
                .map(Optional::of)
                .toList()
        );
    }

    @Override
    public CompletableFuture<StateEntity> AddAsync(StateEntity state) {
        return CompletableFuture.supplyAsync(() -> jpaRepository.save(state));
    }

    @Override
    public CompletableFuture<StateEntity> UpdateAsync(StateEntity state) {
        return CompletableFuture.supplyAsync(() -> jpaRepository.save(state));
    }

    @Override
    public CompletableFuture<StateEntity> DeleteAsync(int id) {
        return CompletableFuture.supplyAsync(() -> {
            Optional<StateEntity> state = jpaRepository.findById(id);
            if (state.isPresent()) {
                jpaRepository.deleteById(id);
                return state.get();
            }
            throw new RuntimeException("State not found with id: " + id);
        });
    }
}