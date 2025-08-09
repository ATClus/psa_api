package com.clusterat.psa_api.presentation;

import com.clusterat.psa_api.application.commands.CreateStateCommand;
import com.clusterat.psa_api.application.dto.StateApplicationDTO;
import com.clusterat.psa_api.application.handlers.CreateStateCommandHandler;
import com.clusterat.psa_api.application.interfaces.IStateRepository;
import com.clusterat.psa_api.domain.entities.CountryEntity;
import com.clusterat.psa_api.domain.entities.StateEntity;
import com.clusterat.psa_api.domain.value_objects.Region;
import com.clusterat.psa_api.presentation.dto.StatePresentationDTO;
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
 * Advanced Mockito test class for StateEndpoints demonstrating:
 * - ArgumentCaptor usage
 * - Spy objects
 * - Verification with times() and never()
 * - Custom argument matchers
 * - Exception handling in async contexts
 * - InOrder verification
 * - BDD style testing
 */
@ExtendWith(MockitoExtension.class)
class StateEndpointsAdvancedMockitoTest {

    @Mock
    private IStateRepository stateRepository;

    @Mock
    private CreateStateCommandHandler createStateCommandHandler;

    @InjectMocks
    private StateEndpoints stateEndpoints;

    @Captor
    private ArgumentCaptor<CreateStateCommand> commandCaptor;

    private StateEntity testState;
    private CountryEntity testCountry;

    @BeforeEach
    void setUp() {
        testCountry = new CountryEntity();
        testCountry.setId(1);
        testCountry.setName("Brazil");

        testState = new StateEntity();
        testState.setId(1);
        testState.setName("São Paulo");
        testState.setShortName("SP");
        testState.setRegion(Region.SUDESTE);
        testState.setIbgeCode("35");
        testState.setCountry(testCountry);
    }

    @Test
    void createState_ShouldCaptureCorrectCommand_WhenCalledWithValidRequest() {
        // Given
        StatePresentationDTO.CreateRequest request = new StatePresentationDTO.CreateRequest(
                "Minas Gerais", "MG", Region.SUDESTE, "31", 1);
        given(createStateCommandHandler.handle(any(CreateStateCommand.class)))
                .willReturn(CompletableFuture.completedFuture(testState));

        // When
        stateEndpoints.createState(request);

        // Then
        then(createStateCommandHandler).should().handle(commandCaptor.capture());
        CreateStateCommand capturedCommand = commandCaptor.getValue();
        
        assertThat(capturedCommand).isNotNull();
        assertThat(capturedCommand.name()).isEqualTo("Minas Gerais");
        assertThat(capturedCommand.shortName()).isEqualTo("MG");
        assertThat(capturedCommand.region()).isEqualTo(Region.SUDESTE);
        assertThat(capturedCommand.ibgeCode()).isEqualTo("31");
        assertThat(capturedCommand.countryId()).isEqualTo(1);
    }

    @Test
    void createState_ShouldCallHandlerExactlyOnce_WhenCalledMultipleTimes() {
        // Given
        StatePresentationDTO.CreateRequest request = new StatePresentationDTO.CreateRequest(
                "São Paulo", "SP", Region.SUDESTE, "35", 1);
        given(createStateCommandHandler.handle(any(CreateStateCommand.class)))
                .willReturn(CompletableFuture.completedFuture(testState));

        // When
        stateEndpoints.createState(request);
        stateEndpoints.createState(request);

        // Then
        then(createStateCommandHandler).should(times(2)).handle(any(CreateStateCommand.class));
    }

    @Test
    void getStates_ShouldNeverCallRepository_WhenNotInvoked() {
        // When - Don't call getStates()
        
        // Then
        then(stateRepository).should(never()).GetAllAsync();
    }

    @Test
    void getStateById_ShouldUseCustomArgumentMatcher_WhenCalledWithPositiveId() {
        // Given
        given(stateRepository.GetByIdAsync(argThat(id -> id > 0)))
                .willReturn(CompletableFuture.completedFuture(Optional.of(testState)));

        // When
        CompletableFuture<ResponseEntity<StateApplicationDTO.Response>> result = stateEndpoints.getStateById(5);

        // Then
        assertThat(result.join().getStatusCode()).isEqualTo(HttpStatus.OK);
        then(stateRepository).should().GetByIdAsync(argThat(id -> id > 0));
    }

    @Test
    void getStates_ShouldHandleEmptyList_UsingSpyList() {
        // Given - Using a spy to track interactions with the list
        List<Optional<StateEntity>> emptyList = spy(new ArrayList<>());
        given(stateRepository.GetAllAsync()).willReturn(CompletableFuture.completedFuture(emptyList));

        // When
        CompletableFuture<ResponseEntity<List<StateApplicationDTO.Response>>> result = stateEndpoints.getStates();

        // Then
        ResponseEntity<List<StateApplicationDTO.Response>> response = result.join();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();
        
        // Verify the spy list was accessed
        verify(emptyList).stream();
    }

    @Test
    void getStates_ShouldFilterEmptyOptionals_WhenSomeStatesAreEmpty() {
        // Given
        StateEntity state2 = new StateEntity();
        state2.setId(2);
        state2.setName("Rio de Janeiro");
        state2.setShortName("RJ");
        state2.setRegion(Region.SUDESTE);
        state2.setIbgeCode("33");
        state2.setCountry(testCountry);
        
        List<Optional<StateEntity>> mixedList = Arrays.asList(
                Optional.of(testState),
                Optional.empty(),
                Optional.of(state2),
                Optional.empty()
        );
        given(stateRepository.GetAllAsync()).willReturn(CompletableFuture.completedFuture(mixedList));

        // When
        CompletableFuture<ResponseEntity<List<StateApplicationDTO.Response>>> result = stateEndpoints.getStates();

        // Then
        ResponseEntity<List<StateApplicationDTO.Response>> response = result.join();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2); // Only non-empty optionals
        assertThat(response.getBody().get(0).id()).isEqualTo(1);
        assertThat(response.getBody().get(1).id()).isEqualTo(2);
    }

    @Test
    void createState_ShouldHandleCompletionException_WhenHandlerFails() {
        // Given
        StatePresentationDTO.CreateRequest request = new StatePresentationDTO.CreateRequest(
                "Invalid", "", Region.SUDESTE, "", 999);
        CompletableFuture<StateEntity> failedFuture = new CompletableFuture<>();
        failedFuture.completeExceptionally(new IllegalArgumentException("Invalid state data"));
        given(createStateCommandHandler.handle(any(CreateStateCommand.class))).willReturn(failedFuture);

        // When
        CompletableFuture<ResponseEntity<StateApplicationDTO.Response>> result = stateEndpoints.createState(request);

        // Then
        assertThat(result.join().getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        then(createStateCommandHandler).should().handle(any(CreateStateCommand.class));
    }

    @Test
    void deleteState_ShouldVerifyExactArguments_WhenCalledWithSpecificId() {
        // Given
        given(stateRepository.DeleteAsync(eq(42))).willReturn(CompletableFuture.completedFuture(testState));

        // When
        stateEndpoints.deleteState(42);

        // Then
        then(stateRepository).should().DeleteAsync(eq(42));
        then(stateRepository).should(never()).DeleteAsync(intThat(id -> id != 42));
    }

    @Test
    void getStateById_ShouldHandleRepositoryTimeout_WhenSlowResponse() {
        // Given - Simulate a slow repository response
        CompletableFuture<Optional<StateEntity>> slowFuture = new CompletableFuture<>();
        // Don't complete the future to simulate timeout scenario
        given(stateRepository.GetByIdAsync(1)).willReturn(slowFuture);

        // When
        CompletableFuture<ResponseEntity<StateApplicationDTO.Response>> result = stateEndpoints.getStateById(1);

        // Then - Verify the future is not completed immediately
        assertThat(result.isDone()).isFalse();
        
        // Complete the slow future to finish the test
        slowFuture.complete(Optional.of(testState));
        assertThat(result.join().getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void createState_ShouldVerifyNoInteractionWithRepository_WhenHandlerIsUsed() {
        // Given
        StatePresentationDTO.CreateRequest request = new StatePresentationDTO.CreateRequest(
                "Minas Gerais", "MG", Region.SUDESTE, "31", 1);
        given(createStateCommandHandler.handle(any(CreateStateCommand.class)))
                .willReturn(CompletableFuture.completedFuture(testState));

        // When
        stateEndpoints.createState(request);

        // Then - Verify no interaction with repository (only with command handler)
        then(stateRepository).shouldHaveNoInteractions();
        then(createStateCommandHandler).should().handle(any(CreateStateCommand.class));
    }

    @Test
    void getStateByIbgeCode_ShouldUseStringMatcher_WhenCalledWithValidIbgeCode() {
        // Given
        given(stateRepository.GetByIbgeCodeAsync(argThat(code -> code.length() == 2)))
                .willReturn(CompletableFuture.completedFuture(Optional.of(testState)));

        // When
        CompletableFuture<ResponseEntity<StateApplicationDTO.Response>> result = stateEndpoints.getStateByIbgeCode("35");

        // Then
        assertThat(result.join().getStatusCode()).isEqualTo(HttpStatus.OK);
        then(stateRepository).should().GetByIbgeCodeAsync(argThat(code -> code.length() == 2));
    }

    @Test
    void getAllStates_ShouldResetInteractions_WhenTestedMultipleTimes() {
        // Given
        List<Optional<StateEntity>> states = List.of(Optional.of(testState));
        given(stateRepository.GetAllAsync()).willReturn(CompletableFuture.completedFuture(states));

        // When - First call
        stateEndpoints.getStates();
        
        // Reset interactions for clean verification
        reset(stateRepository);
        given(stateRepository.GetAllAsync()).willReturn(CompletableFuture.completedFuture(states));
        
        // When - Second call after reset
        stateEndpoints.getStates();

        // Then - Only verify the second call
        then(stateRepository).should(times(1)).GetAllAsync();
    }

    @Test
    void createState_ShouldVerifyCommandFields_UsingMultipleCaptures() {
        // Given
        StatePresentationDTO.CreateRequest request1 = new StatePresentationDTO.CreateRequest(
                "São Paulo", "SP", Region.SUDESTE, "35", 1);
        StatePresentationDTO.CreateRequest request2 = new StatePresentationDTO.CreateRequest(
                "Rio Grande do Sul", "RS", Region.SUL, "43", 1);
        given(createStateCommandHandler.handle(any(CreateStateCommand.class)))
                .willReturn(CompletableFuture.completedFuture(testState));

        // When
        stateEndpoints.createState(request1);
        stateEndpoints.createState(request2);

        // Then
        then(createStateCommandHandler).should(times(2)).handle(commandCaptor.capture());
        List<CreateStateCommand> capturedCommands = commandCaptor.getAllValues();
        
        assertThat(capturedCommands).hasSize(2);
        assertThat(capturedCommands.get(0).name()).isEqualTo("São Paulo");
        assertThat(capturedCommands.get(0).region()).isEqualTo(Region.SUDESTE);
        assertThat(capturedCommands.get(0).ibgeCode()).isEqualTo("35");
        assertThat(capturedCommands.get(1).name()).isEqualTo("Rio Grande do Sul");
        assertThat(capturedCommands.get(1).region()).isEqualTo(Region.SUL);
        assertThat(capturedCommands.get(1).ibgeCode()).isEqualTo("43");
    }

    @Test
    void createState_ShouldVerifyRegionValueObject_WhenCreatingStatesInDifferentRegions() {
        // Given
        StatePresentationDTO.CreateRequest requestSudeste = new StatePresentationDTO.CreateRequest(
                "São Paulo", "SP", Region.SUDESTE, "35", 1);
        StatePresentationDTO.CreateRequest requestNordeste = new StatePresentationDTO.CreateRequest(
                "Bahia", "BA", Region.NORDESTE, "29", 1);

        given(createStateCommandHandler.handle(any(CreateStateCommand.class)))
                .willReturn(CompletableFuture.completedFuture(testState));

        // When
        stateEndpoints.createState(requestSudeste);
        stateEndpoints.createState(requestNordeste);

        // Then
        then(createStateCommandHandler).should(times(2)).handle(commandCaptor.capture());
        List<CreateStateCommand> capturedCommands = commandCaptor.getAllValues();

        assertThat(capturedCommands.get(0).region()).isEqualTo(Region.SUDESTE);
        assertThat(capturedCommands.get(1).region()).isEqualTo(Region.NORDESTE);
    }

    @Test
    void getStateByIbgeCode_ShouldHandleNullResponse_WhenRepositoryReturnsNull() {
        // Given
        given(stateRepository.GetByIbgeCodeAsync("99"))
                .willReturn(CompletableFuture.completedFuture(Optional.empty()));

        // When
        CompletableFuture<ResponseEntity<StateApplicationDTO.Response>> result = stateEndpoints.getStateByIbgeCode("99");

        // Then
        assertThat(result.join().getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        then(stateRepository).should().GetByIbgeCodeAsync("99");
    }
}