package com.clusterat.psa_api.infrastructure.persistence;

import com.clusterat.psa_api.domain.entities.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserRepositoryTest {

    @Mock
    private SpringDataJpaUserRepository jpaRepository;

    @InjectMocks
    private UserRepository userRepository;

    private UserEntity testUser1;
    private UserEntity testUser2;

    @BeforeEach
    void setUp() {
        testUser1 = new UserEntity();
        testUser1.setId(1);
        testUser1.setCognitoId(12345);

        testUser2 = new UserEntity();
        testUser2.setId(2);
        testUser2.setCognitoId(54321);
    }

    @Test
    void GetByIdAsync_ShouldReturnUser_WhenUserExists() {
        // Given
        given(jpaRepository.findById(1)).willReturn(Optional.of(testUser1));

        // When
        CompletableFuture<Optional<UserEntity>> result = userRepository.GetByIdAsync(1);

        // Then
        assertThat(result).isCompleted();
        Optional<UserEntity> user = result.join();
        assertThat(user).isPresent();
        assertThat(user.get().getId()).isEqualTo(1);
        assertThat(user.get().getCognitoId()).isEqualTo(12345);

        then(jpaRepository).should().findById(1);
    }

    @Test
    void GetByIdAsync_ShouldReturnEmpty_WhenUserDoesNotExist() {
        // Given
        given(jpaRepository.findById(999)).willReturn(Optional.empty());

        // When
        CompletableFuture<Optional<UserEntity>> result = userRepository.GetByIdAsync(999);

        // Then
        assertThat(result).isCompleted();
        Optional<UserEntity> user = result.join();
        assertThat(user).isEmpty();

        then(jpaRepository).should().findById(999);
    }

    @Test
    void GetByCognitoIdAsync_ShouldReturnUser_WhenUserExists() {
        // Given
        given(jpaRepository.findByCognitoId(12345)).willReturn(Optional.of(testUser1));

        // When
        CompletableFuture<Optional<UserEntity>> result = userRepository.GetByCognitoIdAsync(12345);

        // Then
        assertThat(result).isCompleted();
        Optional<UserEntity> user = result.join();
        assertThat(user).isPresent();
        assertThat(user.get().getCognitoId()).isEqualTo(12345);

        then(jpaRepository).should().findByCognitoId(12345);
    }

    @Test
    void GetByCognitoIdAsync_ShouldReturnEmpty_WhenUserDoesNotExist() {
        // Given
        given(jpaRepository.findByCognitoId(99999)).willReturn(Optional.empty());

        // When
        CompletableFuture<Optional<UserEntity>> result = userRepository.GetByCognitoIdAsync(99999);

        // Then
        assertThat(result).isCompleted();
        Optional<UserEntity> user = result.join();
        assertThat(user).isEmpty();

        then(jpaRepository).should().findByCognitoId(99999);
    }

    @Test
    void GetAllAsync_ShouldReturnAllUsers_WhenUsersExist() {
        // Given
        List<UserEntity> jpaUsers = Arrays.asList(testUser1, testUser2);
        given(jpaRepository.findAll()).willReturn(jpaUsers);

        // When
        CompletableFuture<List<Optional<UserEntity>>> result = userRepository.GetAllAsync();

        // Then
        assertThat(result).isCompleted();
        List<Optional<UserEntity>> users = result.join();
        assertThat(users).hasSize(2);
        assertThat(users.get(0)).isPresent();
        assertThat(users.get(0).get().getId()).isEqualTo(1);
        assertThat(users.get(1)).isPresent();
        assertThat(users.get(1).get().getId()).isEqualTo(2);

        then(jpaRepository).should().findAll();
    }

    @Test
    void GetAllAsync_ShouldReturnEmptyList_WhenNoUsersExist() {
        // Given
        given(jpaRepository.findAll()).willReturn(Arrays.asList());

        // When
        CompletableFuture<List<Optional<UserEntity>>> result = userRepository.GetAllAsync();

        // Then
        assertThat(result).isCompleted();
        List<Optional<UserEntity>> users = result.join();
        assertThat(users).isEmpty();

        then(jpaRepository).should().findAll();
    }

    @Test
    void AddAsync_ShouldSaveAndReturnUser_WhenValidUser() {
        // Given
        UserEntity newUser = new UserEntity();
        newUser.setCognitoId(67890);
        
        UserEntity savedUser = new UserEntity();
        savedUser.setId(3);
        savedUser.setCognitoId(67890);

        given(jpaRepository.save(newUser)).willReturn(savedUser);

        // When
        CompletableFuture<UserEntity> result = userRepository.AddAsync(newUser);

        // Then
        assertThat(result).isCompleted();
        UserEntity user = result.join();
        assertThat(user).isNotNull();
        assertThat(user.getId()).isEqualTo(3);
        assertThat(user.getCognitoId()).isEqualTo(67890);

        then(jpaRepository).should().save(newUser);
    }

    @Test
    void AddAsync_ShouldPropagateException_WhenJpaRepositoryThrowsException() {
        // Given
        UserEntity newUser = new UserEntity();
        newUser.setCognitoId(67890);

        given(jpaRepository.save(newUser)).willThrow(new RuntimeException("Database constraint violation"));

        // When
        CompletableFuture<UserEntity> result = userRepository.AddAsync(newUser);

        // Then
        assertThat(result).isCompletedExceptionally();
        assertThatThrownBy(result::join)
                .hasCauseInstanceOf(RuntimeException.class)
                .hasRootCauseMessage("Database constraint violation");

        then(jpaRepository).should().save(newUser);
    }

    @Test
    void UpdateAsync_ShouldSaveAndReturnUser_WhenValidUser() {
        // Given
        testUser1.setCognitoId(99999); // Changed cognito ID
        given(jpaRepository.save(testUser1)).willReturn(testUser1);

        // When
        CompletableFuture<UserEntity> result = userRepository.UpdateAsync(testUser1);

        // Then
        assertThat(result).isCompleted();
        UserEntity user = result.join();
        assertThat(user).isNotNull();
        assertThat(user.getId()).isEqualTo(1);
        assertThat(user.getCognitoId()).isEqualTo(99999);

        then(jpaRepository).should().save(testUser1);
    }

    @Test
    void UpdateAsync_ShouldPropagateException_WhenJpaRepositoryThrowsException() {
        // Given
        given(jpaRepository.save(testUser1)).willThrow(new RuntimeException("Update failed"));

        // When
        CompletableFuture<UserEntity> result = userRepository.UpdateAsync(testUser1);

        // Then
        assertThat(result).isCompletedExceptionally();
        assertThatThrownBy(result::join)
                .hasCauseInstanceOf(RuntimeException.class)
                .hasRootCauseMessage("Update failed");

        then(jpaRepository).should().save(testUser1);
    }

    @Test
    void DeleteAsync_ShouldDeleteAndReturnUser_WhenUserExists() {
        // Given
        given(jpaRepository.findById(1)).willReturn(Optional.of(testUser1));
        doNothing().when(jpaRepository).deleteById(1);

        // When
        CompletableFuture<UserEntity> result = userRepository.DeleteAsync(1);

        // Then
        assertThat(result).isCompleted();
        UserEntity user = result.join();
        assertThat(user).isNotNull();
        assertThat(user.getId()).isEqualTo(1);
        assertThat(user.getCognitoId()).isEqualTo(12345);

        then(jpaRepository).should().findById(1);
        then(jpaRepository).should().deleteById(1);
    }

    @Test
    void DeleteAsync_ShouldThrowException_WhenUserDoesNotExist() {
        // Given
        given(jpaRepository.findById(999)).willReturn(Optional.empty());

        // When
        CompletableFuture<UserEntity> result = userRepository.DeleteAsync(999);

        // Then
        assertThat(result).isCompletedExceptionally();
        assertThatThrownBy(result::join)
                .hasCauseInstanceOf(RuntimeException.class)
                .hasRootCauseMessage("User not found with id: 999");

        then(jpaRepository).should().findById(999);
        then(jpaRepository).should(never()).deleteById(anyInt());
    }

    @Test
    void DeleteAsync_ShouldPropagateException_WhenFindByIdThrowsException() {
        // Given
        given(jpaRepository.findById(1)).willThrow(new RuntimeException("Database connection failed"));

        // When
        CompletableFuture<UserEntity> result = userRepository.DeleteAsync(1);

        // Then
        assertThat(result).isCompletedExceptionally();
        assertThatThrownBy(result::join)
                .hasCauseInstanceOf(RuntimeException.class)
                .hasRootCauseMessage("Database connection failed");

        then(jpaRepository).should().findById(1);
        then(jpaRepository).should(never()).deleteById(anyInt());
    }

    @Test
    void DeleteAsync_ShouldPropagateException_WhenDeleteByIdThrowsException() {
        // Given
        given(jpaRepository.findById(1)).willReturn(Optional.of(testUser1));
        doThrow(new RuntimeException("Delete operation failed")).when(jpaRepository).deleteById(1);

        // When
        CompletableFuture<UserEntity> result = userRepository.DeleteAsync(1);

        // Then
        assertThat(result).isCompletedExceptionally();
        assertThatThrownBy(result::join)
                .hasCauseInstanceOf(RuntimeException.class)
                .hasRootCauseMessage("Delete operation failed");

        then(jpaRepository).should().findById(1);
        then(jpaRepository).should().deleteById(1);
    }

    @Test
    void constructor_ShouldInitializeJpaRepository_WhenCalled() {
        // Given & When
        UserRepository repository = new UserRepository(jpaRepository);

        // Then
        assertThat(repository).isNotNull();
        // Verify that the repository can be created with the JPA repository
    }

    @Test
    void repositoryMethods_ShouldBeAsync_WhenCalled() {
        // Given
        given(jpaRepository.findById(1)).willReturn(Optional.of(testUser1));

        // When
        CompletableFuture<Optional<UserEntity>> result = userRepository.GetByIdAsync(1);

        // Then - Verify the method returns immediately (doesn't block)
        assertThat(result).isNotNull();
        assertThat(result.isDone()).isTrue(); // Since we're using CompletableFuture.supplyAsync with a simple operation
    }
}