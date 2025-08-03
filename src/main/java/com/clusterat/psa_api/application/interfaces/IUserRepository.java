package com.clusterat.psa_api.application.interfaces;

import com.clusterat.psa_api.domain.entities.UserEntity;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface IUserRepository {
    CompletableFuture<Optional<UserEntity>> GetByIdAsync(int id);
    CompletableFuture<Optional<UserEntity>> GetByCognitoIdAsync(int cognitoId);
    CompletableFuture<List<Optional<UserEntity>>> GetAllAsync();
    CompletableFuture<UserEntity> AddAsync(UserEntity user);
    CompletableFuture<UserEntity> UpdateAsync(UserEntity user);
    CompletableFuture<UserEntity> DeleteAsync(int id);
}
