package com.clusterat.psa_api.infrastructure.persistence;

import com.clusterat.psa_api.application.interfaces.IUserRepository;
import com.clusterat.psa_api.domain.entities.UserEntity;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Repository
public class UserRepository implements IUserRepository {
    private final SpringDataJpaUserRepository jpaRepository;

    public UserRepository(SpringDataJpaUserRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public CompletableFuture<Optional<UserEntity>> GetByIdAsync(int id) {
        return CompletableFuture.supplyAsync(() -> jpaRepository.findById(id));
    }

    @Override
    public CompletableFuture<Optional<UserEntity>> GetByCognitoIdAsync(int cognitoId) {
        return CompletableFuture.supplyAsync(() -> jpaRepository.findByCognitoId(cognitoId));
    }

    @Override
    public CompletableFuture<List<Optional<UserEntity>>> GetAllAsync() {
        return CompletableFuture.supplyAsync(() ->
            jpaRepository.findAll().stream()
                .map(Optional::of)
                .toList()
        );
    }

    @Override
    public CompletableFuture<UserEntity> AddAsync(UserEntity user) {
        return CompletableFuture.supplyAsync(() -> jpaRepository.save(user));
    }

    @Override
    public CompletableFuture<UserEntity> UpdateAsync(UserEntity user) {
        return CompletableFuture.supplyAsync(() -> jpaRepository.save(user));
    }

    @Override
    public CompletableFuture<UserEntity> DeleteAsync(int id) {
        return CompletableFuture.supplyAsync(() -> {
            Optional<UserEntity> user = jpaRepository.findById(id);
            if (user.isPresent()) {
                jpaRepository.deleteById(id);
                return user.get();
            }
            throw new RuntimeException("User not found with id: " + id);
        });
    }
}
