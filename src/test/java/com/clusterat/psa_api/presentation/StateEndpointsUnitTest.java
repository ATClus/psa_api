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
class StateEndpointsUnitTest {

    @Mock
    private IStateRepository stateRepository;

    @Mock
    private CreateStateCommandHandler createStateCommandHandler;

    @InjectMocks
    private StateEndpoints stateEndpoints;

    private StateEntity testState;
    private CountryEntity testCountry;
    private List<Optional<StateEntity>> testStates;

    @BeforeEach
    void setUp() {
        testCountry = spy(new CountryEntity());
        testCountry.setId(1);
        testCountry.setName("Brazil");

        testState = spy(new StateEntity());
        testState.setId(1);
        testState.setName("São Paulo");
        testState.setShortName("SP");
        testState.setRegion(Region.SUDESTE);
        testState.setIbgeCode("35");
        testState.setCountry(testCountry);

        StateEntity testState2 = spy(new StateEntity());
        testState2.setId(2);
        testState2.setName("Rio de Janeiro");
        testState2.setShortName("RJ");
        testState2.setRegion(Region.SUDESTE);
        testState2.setIbgeCode("33");
        testState2.setCountry(testCountry);

        testStates = Arrays.asList(Optional.of(testState), Optional.of(testState2));
    }

    @Test
    void getStates_ShouldReturnListOfStates_WhenStatesExist() {
        // Given
        given(stateRepository.GetAllAsync()).willReturn(CompletableFuture.completedFuture(testStates));

        // When
        CompletableFuture<ResponseEntity<List<StateApplicationDTO.Response>>> result = stateEndpoints.getStates();

        // Then
        assertThat(result).isCompleted();
        ResponseEntity<List<StateApplicationDTO.Response>> response = result.join();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2);
        assertThat(response.getBody().get(0).id()).isEqualTo(1);
        assertThat(response.getBody().get(0).name()).isEqualTo("São Paulo");
        assertThat(response.getBody().get(0).shortName()).isEqualTo("SP");
        assertThat(response.getBody().get(0).region()).isEqualTo(Region.SUDESTE);
        assertThat(response.getBody().get(0).ibgeCode()).isEqualTo("35");
        assertThat(response.getBody().get(0).countryId()).isEqualTo(1);

        then(stateRepository).should().GetAllAsync();
    }

    @Test
    void getStates_ShouldReturnInternalServerError_WhenRepositoryThrowsException() {
        // Given
        given(stateRepository.GetAllAsync()).willReturn(CompletableFuture.failedFuture(new RuntimeException("Database error")));

        // When
        CompletableFuture<ResponseEntity<List<StateApplicationDTO.Response>>> result = stateEndpoints.getStates();

        // Then
        assertThat(result).isCompleted();
        ResponseEntity<List<StateApplicationDTO.Response>> response = result.join();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNull();

        then(stateRepository).should().GetAllAsync();
    }

    @Test
    void getStateById_ShouldReturnState_WhenStateExists() {
        // Given
        given(stateRepository.GetByIdAsync(1)).willReturn(CompletableFuture.completedFuture(Optional.of(testState)));

        // When
        CompletableFuture<ResponseEntity<StateApplicationDTO.Response>> result = stateEndpoints.getStateById(1);

        // Then
        assertThat(result).isCompleted();
        ResponseEntity<StateApplicationDTO.Response> response = result.join();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isEqualTo(1);
        assertThat(response.getBody().name()).isEqualTo("São Paulo");
        assertThat(response.getBody().shortName()).isEqualTo("SP");
        assertThat(response.getBody().region()).isEqualTo(Region.SUDESTE);
        assertThat(response.getBody().ibgeCode()).isEqualTo("35");
        assertThat(response.getBody().countryId()).isEqualTo(1);

        then(stateRepository).should().GetByIdAsync(1);
    }

    @Test
    void getStateById_ShouldReturnNotFound_WhenStateDoesNotExist() {
        // Given
        given(stateRepository.GetByIdAsync(999)).willReturn(CompletableFuture.completedFuture(Optional.empty()));

        // When
        CompletableFuture<ResponseEntity<StateApplicationDTO.Response>> result = stateEndpoints.getStateById(999);

        // Then
        assertThat(result).isCompleted();
        ResponseEntity<StateApplicationDTO.Response> response = result.join();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNull();

        then(stateRepository).should().GetByIdAsync(999);
    }

    @Test
    void getStateById_ShouldReturnInternalServerError_WhenRepositoryThrowsException() {
        // Given
        given(stateRepository.GetByIdAsync(1)).willReturn(CompletableFuture.failedFuture(new RuntimeException("Database error")));

        // When
        CompletableFuture<ResponseEntity<StateApplicationDTO.Response>> result = stateEndpoints.getStateById(1);

        // Then
        assertThat(result).isCompleted();
        ResponseEntity<StateApplicationDTO.Response> response = result.join();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNull();

        then(stateRepository).should().GetByIdAsync(1);
    }

    @Test
    void getStateByIbgeCode_ShouldReturnState_WhenStateExists() {
        // Given
        given(stateRepository.GetByIbgeCodeAsync("35")).willReturn(CompletableFuture.completedFuture(Optional.of(testState)));

        // When
        CompletableFuture<ResponseEntity<StateApplicationDTO.Response>> result = stateEndpoints.getStateByIbgeCode("35");

        // Then
        assertThat(result).isCompleted();
        ResponseEntity<StateApplicationDTO.Response> response = result.join();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().ibgeCode()).isEqualTo("35");
        assertThat(response.getBody().name()).isEqualTo("São Paulo");

        then(stateRepository).should().GetByIbgeCodeAsync("35");
    }

    @Test
    void getStateByIbgeCode_ShouldReturnNotFound_WhenStateDoesNotExist() {
        // Given
        given(stateRepository.GetByIbgeCodeAsync("99")).willReturn(CompletableFuture.completedFuture(Optional.empty()));

        // When
        CompletableFuture<ResponseEntity<StateApplicationDTO.Response>> result = stateEndpoints.getStateByIbgeCode("99");

        // Then
        assertThat(result).isCompleted();
        ResponseEntity<StateApplicationDTO.Response> response = result.join();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNull();

        then(stateRepository).should().GetByIbgeCodeAsync("99");
    }

    @Test
    void createState_ShouldCreateAndReturnState_WhenValidRequest() {
        // Given
        StatePresentationDTO.CreateRequest request = new StatePresentationDTO.CreateRequest(
                "Minas Gerais", "MG", Region.SUDESTE, "31", 1);
        given(createStateCommandHandler.handle(any(CreateStateCommand.class)))
                .willReturn(CompletableFuture.completedFuture(testState));

        // When
        CompletableFuture<ResponseEntity<StateApplicationDTO.Response>> result = stateEndpoints.createState(request);

        // Then
        assertThat(result).isCompleted();
        ResponseEntity<StateApplicationDTO.Response> response = result.join();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().name()).isEqualTo("São Paulo");
        assertThat(response.getBody().region()).isEqualTo(Region.SUDESTE);

        then(createStateCommandHandler).should().handle(argThat(command -> 
            command.name().equals("Minas Gerais") &&
            command.shortName().equals("MG") &&
            command.region() == Region.SUDESTE &&
            command.ibgeCode().equals("31") &&
            command.countryId() == 1
        ));
    }

    @Test
    void createState_ShouldReturnBadRequest_WhenCommandHandlerThrowsException() {
        // Given
        StatePresentationDTO.CreateRequest request = new StatePresentationDTO.CreateRequest(
                "Invalid", "", Region.SUDESTE, "", 999);
        given(createStateCommandHandler.handle(any(CreateStateCommand.class)))
                .willReturn(CompletableFuture.failedFuture(new IllegalArgumentException("Invalid state data")));

        // When
        CompletableFuture<ResponseEntity<StateApplicationDTO.Response>> result = stateEndpoints.createState(request);

        // Then
        assertThat(result).isCompleted();
        ResponseEntity<StateApplicationDTO.Response> response = result.join();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNull();

        then(createStateCommandHandler).should().handle(any(CreateStateCommand.class));
    }

    @Test
    void deleteState_ShouldReturnNoContent_WhenStateDeleted() {
        // Given
        given(stateRepository.DeleteAsync(1)).willReturn(CompletableFuture.completedFuture(testState));

        // When
        CompletableFuture<ResponseEntity<Void>> result = stateEndpoints.deleteState(1);

        // Then
        assertThat(result).isCompleted();
        ResponseEntity<Void> response = result.join();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();

        then(stateRepository).should().DeleteAsync(1);
    }

    @Test
    void deleteState_ShouldReturnNotFound_WhenDeleteFails() {
        // Given
        given(stateRepository.DeleteAsync(999)).willReturn(CompletableFuture.failedFuture(new RuntimeException("State not found")));

        // When
        CompletableFuture<ResponseEntity<Void>> result = stateEndpoints.deleteState(999);

        // Then
        assertThat(result).isCompleted();
        ResponseEntity<Void> response = result.join();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNull();

        then(stateRepository).should().DeleteAsync(999);
    }
}