package com.clusterat.psa_api.presentation;

import com.clusterat.psa_api.application.commands.CreteUserCommand;
import com.clusterat.psa_api.application.dto.UserApplicationDTO;
import com.clusterat.psa_api.application.handlers.CreateUserCommandHandler;
import com.clusterat.psa_api.application.interfaces.IUserRepository;
import com.clusterat.psa_api.domain.entities.UserEntity;
import com.clusterat.psa_api.presentation.dto.UserPresentationDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserEndpointsUnitTest {

    @Mock
    private IUserRepository userRepository;

    @Mock
    private CreateUserCommandHandler createUserCommandHandler;

    @InjectMocks
    private UserEndpoints userEndpoints;

    private UserEntity testUser;
    private List<Optional<UserEntity>> testUsers;

    @BeforeEach
    void setUp() {
        testUser = spy(new UserEntity());
        testUser.setId(1);
        testUser.setCognitoId(12345);

        UserEntity testUser2 = spy(new UserEntity());
        testUser2.setId(2);
        testUser2.setCognitoId(54321);

        testUsers = Arrays.asList(Optional.of(testUser), Optional.of(testUser2));
    }

    @Test
    void getUsers_ShouldReturnListOfUsers_WhenUsersExist() {
        // Given
        given(userRepository.GetAllAsync()).willReturn(CompletableFuture.completedFuture(testUsers));

        // When
        CompletableFuture<ResponseEntity<List<UserApplicationDTO.Response>>> result = userEndpoints.getUsers();

        // Then
        assertThat(result).isCompleted();
        ResponseEntity<List<UserApplicationDTO.Response>> response = result.join();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2);
        assertThat(response.getBody().get(0).id()).isEqualTo(1);
        assertThat(response.getBody().get(0).cognitoId()).isEqualTo(12345);

        then(userRepository).should().GetAllAsync();
    }

    @Test
    void getUsers_ShouldReturnInternalServerError_WhenRepositoryThrowsException() {
        // Given
        given(userRepository.GetAllAsync()).willReturn(CompletableFuture.failedFuture(new RuntimeException("Database error")));

        // When
        CompletableFuture<ResponseEntity<List<UserApplicationDTO.Response>>> result = userEndpoints.getUsers();

        // Then
        assertThat(result).isCompleted();
        ResponseEntity<List<UserApplicationDTO.Response>> response = result.join();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNull();

        then(userRepository).should().GetAllAsync();
    }

    @Test
    void getUserById_ShouldReturnUser_WhenUserExists() {
        // Given
        given(userRepository.GetByIdAsync(1)).willReturn(CompletableFuture.completedFuture(Optional.of(testUser)));

        // When
        CompletableFuture<ResponseEntity<UserApplicationDTO.Response>> result = userEndpoints.getUserById(1);

        // Then
        assertThat(result).isCompleted();
        ResponseEntity<UserApplicationDTO.Response> response = result.join();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isEqualTo(1);
        assertThat(response.getBody().cognitoId()).isEqualTo(12345);

        then(userRepository).should().GetByIdAsync(1);
    }

    @Test
    void getUserById_ShouldReturnNotFound_WhenUserDoesNotExist() {
        // Given
        given(userRepository.GetByIdAsync(999)).willReturn(CompletableFuture.completedFuture(Optional.empty()));

        // When
        CompletableFuture<ResponseEntity<UserApplicationDTO.Response>> result = userEndpoints.getUserById(999);

        // Then
        assertThat(result).isCompleted();
        ResponseEntity<UserApplicationDTO.Response> response = result.join();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNull();

        then(userRepository).should().GetByIdAsync(999);
    }

    @Test
    void getUserByCognitoId_ShouldReturnUser_WhenUserExists() {
        // Given
        given(userRepository.GetByCognitoIdAsync(12345)).willReturn(CompletableFuture.completedFuture(Optional.of(testUser)));

        // When
        CompletableFuture<ResponseEntity<UserApplicationDTO.Response>> result = userEndpoints.getUserByCognitoId(12345);

        // Then
        assertThat(result).isCompleted();
        ResponseEntity<UserApplicationDTO.Response> response = result.join();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().cognitoId()).isEqualTo(12345);

        then(userRepository).should().GetByCognitoIdAsync(12345);
    }

    @Test
    void createUser_ShouldCreateAndReturnUser_WhenValidRequest() {
        // Given
        UserPresentationDTO.CreateRequest request = new UserPresentationDTO.CreateRequest(12345);
        given(createUserCommandHandler.handle(any(CreteUserCommand.class))).willReturn(CompletableFuture.completedFuture(testUser));

        // When
        CompletableFuture<ResponseEntity<UserApplicationDTO.Response>> result = userEndpoints.createUser(request);

        // Then
        assertThat(result).isCompleted();
        ResponseEntity<UserApplicationDTO.Response> response = result.join();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().cognitoId()).isEqualTo(12345);

        then(createUserCommandHandler).should().handle(argThat(command -> 
            command.cognitoId() == 12345
        ));
    }

    @Test
    void createUser_ShouldReturnBadRequest_WhenCommandHandlerThrowsException() {
        // Given
        UserPresentationDTO.CreateRequest request = new UserPresentationDTO.CreateRequest(12345);
        given(createUserCommandHandler.handle(any(CreteUserCommand.class)))
                .willReturn(CompletableFuture.failedFuture(new IllegalArgumentException("Invalid cognito ID")));

        // When
        CompletableFuture<ResponseEntity<UserApplicationDTO.Response>> result = userEndpoints.createUser(request);

        // Then
        assertThat(result).isCompleted();
        ResponseEntity<UserApplicationDTO.Response> response = result.join();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNull();

        then(createUserCommandHandler).should().handle(any(CreteUserCommand.class));
    }

    @Test
    void updateUser_ShouldUpdateAndReturnUser_WhenUserExists() {
        // Given
        UserPresentationDTO.CreateRequest request = new UserPresentationDTO.CreateRequest(54321);
        UserEntity updatedUser = spy(new UserEntity());
        updatedUser.setId(1);
        updatedUser.setCognitoId(54321);
        
        given(userRepository.GetByIdAsync(1)).willReturn(CompletableFuture.completedFuture(Optional.of(testUser)));
        given(userRepository.UpdateAsync(testUser)).willReturn(CompletableFuture.completedFuture(updatedUser));

        // When
        CompletableFuture<ResponseEntity<UserApplicationDTO.Response>> result = userEndpoints.updateUser(1, request);

        // Then
        assertThat(result).isCompleted();
        ResponseEntity<UserApplicationDTO.Response> response = result.join();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().cognitoId()).isEqualTo(54321);

        then(userRepository).should().GetByIdAsync(1);
        then(testUser).should().setCognitoId(54321);
        then(userRepository).should().UpdateAsync(testUser);
    }

    @Test
    void updateUser_ShouldReturnNotFound_WhenUserDoesNotExist() {
        // Given
        UserPresentationDTO.CreateRequest request = new UserPresentationDTO.CreateRequest(54321);
        given(userRepository.GetByIdAsync(999)).willReturn(CompletableFuture.completedFuture(Optional.empty()));

        // When
        CompletableFuture<ResponseEntity<UserApplicationDTO.Response>> result = userEndpoints.updateUser(999, request);

        // Then
        assertThat(result).isCompleted();
        ResponseEntity<UserApplicationDTO.Response> response = result.join();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNull();

        then(userRepository).should().GetByIdAsync(999);
        then(userRepository).should(never()).UpdateAsync(any());
    }

    @Test
    void deleteUser_ShouldReturnNoContent_WhenUserDeleted() {
        // Given
        given(userRepository.DeleteAsync(1)).willReturn(CompletableFuture.completedFuture(testUser));

        // When
        CompletableFuture<ResponseEntity<Void>> result = userEndpoints.deleteUser(1);

        // Then
        assertThat(result).isCompleted();
        ResponseEntity<Void> response = result.join();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();

        then(userRepository).should().DeleteAsync(1);
    }

    @Test
    void deleteUser_ShouldReturnNotFound_WhenDeleteFails() {
        // Given
        given(userRepository.DeleteAsync(999)).willReturn(CompletableFuture.failedFuture(new RuntimeException("User not found")));

        // When
        CompletableFuture<ResponseEntity<Void>> result = userEndpoints.deleteUser(999);

        // Then
        assertThat(result).isCompleted();
        ResponseEntity<Void> response = result.join();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNull();

        then(userRepository).should().DeleteAsync(999);
    }
}