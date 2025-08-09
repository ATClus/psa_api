package com.clusterat.psa_api.application.handlers;

import com.clusterat.psa_api.application.commands.CreteUserCommand;
import com.clusterat.psa_api.application.interfaces.IUserRepository;
import com.clusterat.psa_api.domain.entities.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateUserCommandHandlerTest {

    @Mock
    private IUserRepository userRepository;

    @InjectMocks
    private CreateUserCommandHandler commandHandler;

    private UserEntity testUser;
    private CreteUserCommand testCommand;

    @BeforeEach
    void setUp() {
        testUser = new UserEntity();
        testUser.setId(1);
        testUser.setCognitoId(12345);
        
        testCommand = new CreteUserCommand(12345);
    }

    @Test
    void handle_ShouldCreateUserSuccessfully_WhenValidCommand() {
        // Given
        try (MockedStatic<UserEntity> mockedStatic = mockStatic(UserEntity.class)) {
            mockedStatic.when(() -> UserEntity.create(12345)).thenReturn(testUser);
            given(userRepository.AddAsync(testUser)).willReturn(CompletableFuture.completedFuture(testUser));

            // When
            CompletableFuture<UserEntity> result = commandHandler.handle(testCommand);

            // Then
            assertThat(result).isCompleted();
            UserEntity createdUser = result.join();
            assertThat(createdUser).isNotNull();
            assertThat(createdUser.getId()).isEqualTo(1);
            assertThat(createdUser.getCognitoId()).isEqualTo(12345);

            mockedStatic.verify(() -> UserEntity.create(12345));
            then(userRepository).should().AddAsync(testUser);
        }
    }

    @Test
    void handle_ShouldPropagateException_WhenUserEntityCreationFails() {
        // Given
        try (MockedStatic<UserEntity> mockedStatic = mockStatic(UserEntity.class)) {
            mockedStatic.when(() -> UserEntity.create(12345))
                    .thenThrow(new IllegalArgumentException("Cognito ID must be positive"));

            // When & Then
            assertThatThrownBy(() -> commandHandler.handle(testCommand))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Cognito ID must be positive");

            mockedStatic.verify(() -> UserEntity.create(12345));
            then(userRepository).should(never()).AddAsync(any());
        }
    }

    @Test
    void handle_ShouldPropagateException_WhenRepositoryFails() {
        // Given
        try (MockedStatic<UserEntity> mockedStatic = mockStatic(UserEntity.class)) {
            mockedStatic.when(() -> UserEntity.create(12345)).thenReturn(testUser);
            given(userRepository.AddAsync(testUser))
                    .willReturn(CompletableFuture.failedFuture(new RuntimeException("Database connection failed")));

            // When
            CompletableFuture<UserEntity> result = commandHandler.handle(testCommand);

            // Then
            assertThat(result).isCompletedExceptionally();
            assertThatThrownBy(result::join)
                    .hasCauseInstanceOf(RuntimeException.class)
                    .hasRootCauseMessage("Database connection failed");

            mockedStatic.verify(() -> UserEntity.create(12345));
            then(userRepository).should().AddAsync(testUser);
        }
    }

    @Test
    void handle_ShouldCreateUserWithZeroId_WhenNewEntity() {
        // Given
        UserEntity newUser = new UserEntity(); // New entity with ID = 0
        newUser.setCognitoId(12345);
        
        UserEntity persistedUser = new UserEntity(); // After persistence with assigned ID
        persistedUser.setId(5);
        persistedUser.setCognitoId(12345);

        try (MockedStatic<UserEntity> mockedStatic = mockStatic(UserEntity.class)) {
            mockedStatic.when(() -> UserEntity.create(12345)).thenReturn(newUser);
            given(userRepository.AddAsync(newUser)).willReturn(CompletableFuture.completedFuture(persistedUser));

            // When
            CompletableFuture<UserEntity> result = commandHandler.handle(testCommand);

            // Then
            assertThat(result).isCompleted();
            UserEntity createdUser = result.join();
            assertThat(createdUser).isNotNull();
            assertThat(createdUser.getId()).isEqualTo(5); // ID assigned by database
            assertThat(createdUser.getCognitoId()).isEqualTo(12345);

            mockedStatic.verify(() -> UserEntity.create(12345));
            then(userRepository).should().AddAsync(newUser);
        }
    }

    @Test
    void handle_ShouldHandleNegativeCognitoId_WhenInvalidCommand() {
        // Given
        CreteUserCommand invalidCommand = new CreteUserCommand(-1);

        try (MockedStatic<UserEntity> mockedStatic = mockStatic(UserEntity.class)) {
            mockedStatic.when(() -> UserEntity.create(-1))
                    .thenThrow(new IllegalArgumentException("Cognito ID must be positive"));

            // When & Then
            assertThatThrownBy(() -> commandHandler.handle(invalidCommand))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Cognito ID must be positive");

            mockedStatic.verify(() -> UserEntity.create(-1));
            then(userRepository).should(never()).AddAsync(any());
        }
    }

    @Test
    void constructor_ShouldInitializeRepository_WhenCalled() {
        // Given & When
        CreateUserCommandHandler handler = new CreateUserCommandHandler(userRepository);

        // Then
        assertThat(handler).isNotNull();
        // Verify that the handler can be created with the repository
    }
}