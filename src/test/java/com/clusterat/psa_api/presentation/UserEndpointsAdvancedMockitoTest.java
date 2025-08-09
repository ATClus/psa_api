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
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

/**
 * Advanced Mockito test class demonstrating:
 * - ArgumentCaptor usage
 * - Spy objects
 * - Verification with times() and never()
 * - Custom argument matchers
 * - Exception handling in async contexts
 * - BDD style testing
 */
@ExtendWith(MockitoExtension.class)
class UserEndpointsAdvancedMockitoTest {

    @Mock
    private IUserRepository userRepository;

    @Mock
    private CreateUserCommandHandler createUserCommandHandler;

    @InjectMocks
    private UserEndpoints userEndpoints;

    @Captor
    private ArgumentCaptor<CreteUserCommand> commandCaptor;

    private UserEntity testUser;

    @BeforeEach
    void setUp() {
        testUser = new UserEntity();
        testUser.setId(1);
        testUser.setCognitoId(12345);
    }

    @Test
    void createUser_ShouldCaptureCorrectCommand_WhenCalledWithValidRequest() {
        // Given
        UserPresentationDTO.CreateRequest request = new UserPresentationDTO.CreateRequest(98765);
        given(createUserCommandHandler.handle(any(CreteUserCommand.class)))
                .willReturn(CompletableFuture.completedFuture(testUser));

        // When
        userEndpoints.createUser(request);

        // Then
        then(createUserCommandHandler).should().handle(commandCaptor.capture());
        CreteUserCommand capturedCommand = commandCaptor.getValue();
        
        assertThat(capturedCommand).isNotNull();
        assertThat(capturedCommand.cognitoId()).isEqualTo(98765);
    }

    @Test
    void createUser_ShouldCallHandlerExactlyOnce_WhenCalledMultipleTimes() {
        // Given
        UserPresentationDTO.CreateRequest request = new UserPresentationDTO.CreateRequest(12345);
        given(createUserCommandHandler.handle(any(CreteUserCommand.class)))
                .willReturn(CompletableFuture.completedFuture(testUser));

        // When
        userEndpoints.createUser(request);
        userEndpoints.createUser(request);

        // Then
        then(createUserCommandHandler).should(times(2)).handle(any(CreteUserCommand.class));
    }

    @Test
    void getUsers_ShouldNeverCallRepository_WhenNotInvoked() {
        // When - Don't call getUsers()
        
        // Then
        then(userRepository).should(never()).GetAllAsync();
    }

    @Test
    void getUserById_ShouldUseCustomArgumentMatcher_WhenCalledWithPositiveId() {
        // Given
        given(userRepository.GetByIdAsync(argThat(id -> id > 0)))
                .willReturn(CompletableFuture.completedFuture(Optional.of(testUser)));

        // When
        CompletableFuture<ResponseEntity<UserApplicationDTO.Response>> result = userEndpoints.getUserById(5);

        // Then
        assertThat(result.join().getStatusCode()).isEqualTo(HttpStatus.OK);
        then(userRepository).should().GetByIdAsync(argThat(id -> id > 0));
    }

    @Test
    void getUsers_ShouldHandleEmptyList_UsingSpyList() {
        // Given - Using a spy to track interactions with the list
        List<Optional<UserEntity>> emptyList = spy(new ArrayList<>());
        given(userRepository.GetAllAsync()).willReturn(CompletableFuture.completedFuture(emptyList));

        // When
        CompletableFuture<ResponseEntity<List<UserApplicationDTO.Response>>> result = userEndpoints.getUsers();

        // Then
        ResponseEntity<List<UserApplicationDTO.Response>> response = result.join();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();
        
        // Verify the spy list was accessed
        verify(emptyList).stream();
    }

    @Test
    void getUsers_ShouldFilterEmptyOptionals_WhenSomeUsersAreEmpty() {
        // Given
        UserEntity user2 = new UserEntity();
        user2.setId(2);
        user2.setCognitoId(54321);
        
        List<Optional<UserEntity>> mixedList = Arrays.asList(
                Optional.of(testUser),
                Optional.empty(),
                Optional.of(user2),
                Optional.empty()
        );
        given(userRepository.GetAllAsync()).willReturn(CompletableFuture.completedFuture(mixedList));

        // When
        CompletableFuture<ResponseEntity<List<UserApplicationDTO.Response>>> result = userEndpoints.getUsers();

        // Then
        ResponseEntity<List<UserApplicationDTO.Response>> response = result.join();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2); // Only non-empty optionals
        assertThat(response.getBody().get(0).id()).isEqualTo(1);
        assertThat(response.getBody().get(1).id()).isEqualTo(2);
    }

    @Test
    void createUser_ShouldHandleCompletionException_WhenHandlerFails() {
        // Given
        UserPresentationDTO.CreateRequest request = new UserPresentationDTO.CreateRequest(12345);
        CompletableFuture<UserEntity> failedFuture = new CompletableFuture<>();
        failedFuture.completeExceptionally(new IllegalArgumentException("Invalid cognito ID"));
        given(createUserCommandHandler.handle(any(CreteUserCommand.class))).willReturn(failedFuture);

        // When
        CompletableFuture<ResponseEntity<UserApplicationDTO.Response>> result = userEndpoints.createUser(request);

        // Then
        assertThat(result.join().getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        then(createUserCommandHandler).should().handle(any(CreteUserCommand.class));
    }

    @Test
    void updateUser_ShouldCallRepositoryInCorrectOrder_WhenUpdatingUser() {
        // Given
        UserPresentationDTO.CreateRequest request = new UserPresentationDTO.CreateRequest(99999);
        UserEntity updatedUser = new UserEntity();
        updatedUser.setId(1);
        updatedUser.setCognitoId(99999);

        given(userRepository.GetByIdAsync(1)).willReturn(CompletableFuture.completedFuture(Optional.of(testUser)));
        given(userRepository.UpdateAsync(testUser)).willReturn(CompletableFuture.completedFuture(updatedUser));

        // When
        userEndpoints.updateUser(1, request);

        // Then - Verify method call order
        InOrder inOrder = inOrder(userRepository);
        inOrder.verify(userRepository).GetByIdAsync(1);
        inOrder.verify(userRepository).UpdateAsync(testUser);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    void deleteUser_ShouldVerifyExactArguments_WhenCalledWithSpecificId() {
        // Given
        given(userRepository.DeleteAsync(eq(42))).willReturn(CompletableFuture.completedFuture(testUser));

        // When
        userEndpoints.deleteUser(42);

        // Then
        then(userRepository).should().DeleteAsync(eq(42));
        then(userRepository).should(never()).DeleteAsync(intThat(id -> id != 42));
    }

    @Test
    void getUserById_ShouldHandleRepositoryTimeout_WhenSlowResponse() {
        // Given - Simulate a slow repository response
        CompletableFuture<Optional<UserEntity>> slowFuture = new CompletableFuture<>();
        // Don't complete the future to simulate timeout scenario
        given(userRepository.GetByIdAsync(1)).willReturn(slowFuture);

        // When
        CompletableFuture<ResponseEntity<UserApplicationDTO.Response>> result = userEndpoints.getUserById(1);

        // Then - Verify the future is not completed immediately
        assertThat(result.isDone()).isFalse();
        
        // Complete the slow future to finish the test
        slowFuture.complete(Optional.of(testUser));
        assertThat(result.join().getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void createUser_ShouldVerifyNoInteractionWithRepository_WhenHandlerIsUsed() {
        // Given
        UserPresentationDTO.CreateRequest request = new UserPresentationDTO.CreateRequest(12345);
        given(createUserCommandHandler.handle(any(CreteUserCommand.class)))
                .willReturn(CompletableFuture.completedFuture(testUser));

        // When
        userEndpoints.createUser(request);

        // Then - Verify no interaction with repository (only with command handler)
        then(userRepository).shouldHaveNoInteractions();
        then(createUserCommandHandler).should().handle(any(CreteUserCommand.class));
    }

    @Test
    void getAllUsers_ShouldResetInteractions_WhenTestedMultipleTimes() {
        // Given
        List<Optional<UserEntity>> users = List.of(Optional.of(testUser));
        given(userRepository.GetAllAsync()).willReturn(CompletableFuture.completedFuture(users));

        // When - First call
        userEndpoints.getUsers();
        
        // Reset interactions for clean verification
        reset(userRepository);
        given(userRepository.GetAllAsync()).willReturn(CompletableFuture.completedFuture(users));
        
        // When - Second call after reset
        userEndpoints.getUsers();

        // Then - Only verify the second call
        then(userRepository).should(times(1)).GetAllAsync();
    }
}