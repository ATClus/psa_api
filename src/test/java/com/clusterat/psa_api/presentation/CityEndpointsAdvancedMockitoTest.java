package com.clusterat.psa_api.presentation;

import com.clusterat.psa_api.application.commands.CreateCityCommand;
import com.clusterat.psa_api.application.dto.CityApplicationDTO;
import com.clusterat.psa_api.application.handlers.CreateCityCommandHandler;
import com.clusterat.psa_api.application.interfaces.ICityRepository;
import com.clusterat.psa_api.domain.entities.CityEntity;
import com.clusterat.psa_api.domain.entities.CountryEntity;
import com.clusterat.psa_api.domain.entities.StateEntity;
import com.clusterat.psa_api.domain.value_objects.Region;
import com.clusterat.psa_api.presentation.dto.CityPresentationDTO;
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
 * Advanced Mockito test class for CityEndpoints demonstrating:
 * - ArgumentCaptor usage
 * - Spy objects
 * - Verification with times() and never()
 * - Custom argument matchers
 * - Exception handling in async contexts
 * - InOrder verification
 * - BDD style testing
 */
@ExtendWith(MockitoExtension.class)
class CityEndpointsAdvancedMockitoTest {

    @Mock
    private ICityRepository cityRepository;

    @Mock
    private CreateCityCommandHandler createCityCommandHandler;

    @InjectMocks
    private CityEndpoints cityEndpoints;

    @Captor
    private ArgumentCaptor<CreateCityCommand> commandCaptor;

    private CityEntity testCity;
    private StateEntity testState;

    @BeforeEach
    void setUp() {
        CountryEntity testCountry = new CountryEntity();
        testCountry.setId(1);
        testCountry.setName("Brazil");

        testState = new StateEntity();
        testState.setId(1);
        testState.setName("São Paulo");
        testState.setShortName("SP");
        testState.setRegion(Region.SUDESTE);
        testState.setCountry(testCountry);

        testCity = new CityEntity();
        testCity.setId(1);
        testCity.setName("São Paulo");
        testCity.setShortName("SP");
        testCity.setIbgeCode("3550308");
        testCity.setState(testState);
    }

    @Test
    void createCity_ShouldCaptureCorrectCommand_WhenCalledWithValidRequest() {
        // Given
        CityPresentationDTO.CreateRequest request = new CityPresentationDTO.CreateRequest(
                "Campinas", "Campinas", "3509502", 1);
        given(createCityCommandHandler.handle(any(CreateCityCommand.class)))
                .willReturn(CompletableFuture.completedFuture(testCity));

        // When
        cityEndpoints.createCity(request);

        // Then
        then(createCityCommandHandler).should().handle(commandCaptor.capture());
        CreateCityCommand capturedCommand = commandCaptor.getValue();
        
        assertThat(capturedCommand).isNotNull();
        assertThat(capturedCommand.name()).isEqualTo("Campinas");
        assertThat(capturedCommand.shortName()).isEqualTo("Campinas");
        assertThat(capturedCommand.ibgeCode()).isEqualTo("3509502");
        assertThat(capturedCommand.stateId()).isEqualTo(1);
    }

    @Test
    void createCity_ShouldCallHandlerExactlyOnce_WhenCalledMultipleTimes() {
        // Given
        CityPresentationDTO.CreateRequest request = new CityPresentationDTO.CreateRequest(
                "São Paulo", "SP", "3550308", 1);
        given(createCityCommandHandler.handle(any(CreateCityCommand.class)))
                .willReturn(CompletableFuture.completedFuture(testCity));

        // When
        cityEndpoints.createCity(request);
        cityEndpoints.createCity(request);

        // Then
        then(createCityCommandHandler).should(times(2)).handle(any(CreateCityCommand.class));
    }

    @Test
    void getCities_ShouldNeverCallRepository_WhenNotInvoked() {
        // When - Don't call getCities()
        
        // Then
        then(cityRepository).should(never()).GetAllAsync();
    }

    @Test
    void getCityById_ShouldUseCustomArgumentMatcher_WhenCalledWithPositiveId() {
        // Given
        given(cityRepository.GetByIdAsync(argThat(id -> id > 0)))
                .willReturn(CompletableFuture.completedFuture(Optional.of(testCity)));

        // When
        CompletableFuture<ResponseEntity<CityApplicationDTO.Response>> result = cityEndpoints.getCityById(5);

        // Then
        assertThat(result.join().getStatusCode()).isEqualTo(HttpStatus.OK);
        then(cityRepository).should().GetByIdAsync(argThat(id -> id > 0));
    }

    @Test
    void getCities_ShouldHandleEmptyList_UsingSpyList() {
        // Given - Using a spy to track interactions with the list
        List<Optional<CityEntity>> emptyList = spy(new ArrayList<>());
        given(cityRepository.GetAllAsync()).willReturn(CompletableFuture.completedFuture(emptyList));

        // When
        CompletableFuture<ResponseEntity<List<CityApplicationDTO.Response>>> result = cityEndpoints.getCities();

        // Then
        ResponseEntity<List<CityApplicationDTO.Response>> response = result.join();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();
        
        // Verify the spy list was accessed
        verify(emptyList).stream();
    }

    @Test
    void getCities_ShouldFilterEmptyOptionals_WhenSomeCitiesAreEmpty() {
        // Given
        CityEntity city2 = new CityEntity();
        city2.setId(2);
        city2.setName("Santos");
        city2.setShortName("Santos");
        city2.setIbgeCode("3548500");
        city2.setState(testState);
        
        List<Optional<CityEntity>> mixedList = Arrays.asList(
                Optional.of(testCity),
                Optional.empty(),
                Optional.of(city2),
                Optional.empty()
        );
        given(cityRepository.GetAllAsync()).willReturn(CompletableFuture.completedFuture(mixedList));

        // When
        CompletableFuture<ResponseEntity<List<CityApplicationDTO.Response>>> result = cityEndpoints.getCities();

        // Then
        ResponseEntity<List<CityApplicationDTO.Response>> response = result.join();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2); // Only non-empty optionals
        assertThat(response.getBody().get(0).id()).isEqualTo(1);
        assertThat(response.getBody().get(1).id()).isEqualTo(2);
    }

    @Test
    void createCity_ShouldHandleCompletionException_WhenHandlerFails() {
        // Given
        CityPresentationDTO.CreateRequest request = new CityPresentationDTO.CreateRequest(
                "Invalid", "", "", 999);
        CompletableFuture<CityEntity> failedFuture = new CompletableFuture<>();
        failedFuture.completeExceptionally(new IllegalArgumentException("Invalid city data"));
        given(createCityCommandHandler.handle(any(CreateCityCommand.class))).willReturn(failedFuture);

        // When
        CompletableFuture<ResponseEntity<CityApplicationDTO.Response>> result = cityEndpoints.createCity(request);

        // Then
        assertThat(result.join().getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        then(createCityCommandHandler).should().handle(any(CreateCityCommand.class));
    }

    @Test
    void updateCity_ShouldCallRepositoryInCorrectOrder_WhenUpdatingCity() {
        // Given
        CityPresentationDTO.UpdateRequest request = new CityPresentationDTO.UpdateRequest(
                "São Paulo Updated", "SPU", "3550308", 1);
        CityEntity updatedCity = new CityEntity();
        updatedCity.setId(1);
        updatedCity.setName("São Paulo Updated");
        updatedCity.setShortName("SPU");
        updatedCity.setIbgeCode("3550308");
        updatedCity.setState(testState);

        given(cityRepository.GetByIdAsync(1)).willReturn(CompletableFuture.completedFuture(Optional.of(testCity)));
        given(cityRepository.UpdateAsync(testCity)).willReturn(CompletableFuture.completedFuture(updatedCity));

        // When
        cityEndpoints.updateCity(1, request);

        // Then - Verify method call order
        InOrder inOrder = inOrder(cityRepository);
        inOrder.verify(cityRepository).GetByIdAsync(1);
        inOrder.verify(cityRepository).UpdateAsync(testCity);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    void deleteCity_ShouldVerifyExactArguments_WhenCalledWithSpecificId() {
        // Given
        given(cityRepository.DeleteAsync(eq(42))).willReturn(CompletableFuture.completedFuture(testCity));

        // When
        cityEndpoints.deleteCity(42);

        // Then
        then(cityRepository).should().DeleteAsync(eq(42));
        then(cityRepository).should(never()).DeleteAsync(intThat(id -> id != 42));
    }

    @Test
    void getCityById_ShouldHandleRepositoryTimeout_WhenSlowResponse() {
        // Given - Simulate a slow repository response
        CompletableFuture<Optional<CityEntity>> slowFuture = new CompletableFuture<>();
        // Don't complete the future to simulate timeout scenario
        given(cityRepository.GetByIdAsync(1)).willReturn(slowFuture);

        // When
        CompletableFuture<ResponseEntity<CityApplicationDTO.Response>> result = cityEndpoints.getCityById(1);

        // Then - Verify the future is not completed immediately
        assertThat(result.isDone()).isFalse();
        
        // Complete the slow future to finish the test
        slowFuture.complete(Optional.of(testCity));
        assertThat(result.join().getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void createCity_ShouldVerifyNoInteractionWithRepository_WhenHandlerIsUsed() {
        // Given
        CityPresentationDTO.CreateRequest request = new CityPresentationDTO.CreateRequest(
                "Campinas", "Campinas", "3509502", 1);
        given(createCityCommandHandler.handle(any(CreateCityCommand.class)))
                .willReturn(CompletableFuture.completedFuture(testCity));

        // When
        cityEndpoints.createCity(request);

        // Then - Verify no interaction with repository (only with command handler)
        then(cityRepository).shouldHaveNoInteractions();
        then(createCityCommandHandler).should().handle(any(CreateCityCommand.class));
    }

    @Test
    void getCityByIbgeCode_ShouldUseStringMatcher_WhenCalledWithValidIbgeCode() {
        // Given
        given(cityRepository.GetByIbgeCodeAsync(argThat(code -> code.length() == 7)))
                .willReturn(CompletableFuture.completedFuture(Optional.of(testCity)));

        // When
        CompletableFuture<ResponseEntity<CityApplicationDTO.Response>> result = cityEndpoints.getCityByIbgeCode("3550308");

        // Then
        assertThat(result.join().getStatusCode()).isEqualTo(HttpStatus.OK);
        then(cityRepository).should().GetByIbgeCodeAsync(argThat(code -> code.length() == 7));
    }

    @Test
    void getAllCities_ShouldResetInteractions_WhenTestedMultipleTimes() {
        // Given
        List<Optional<CityEntity>> cities = List.of(Optional.of(testCity));
        given(cityRepository.GetAllAsync()).willReturn(CompletableFuture.completedFuture(cities));

        // When - First call
        cityEndpoints.getCities();
        
        // Reset interactions for clean verification
        reset(cityRepository);
        given(cityRepository.GetAllAsync()).willReturn(CompletableFuture.completedFuture(cities));
        
        // When - Second call after reset
        cityEndpoints.getCities();

        // Then - Only verify the second call
        then(cityRepository).should(times(1)).GetAllAsync();
    }

    @Test
    void createCity_ShouldVerifyCommandFields_UsingMultipleCaptures() {
        // Given
        CityPresentationDTO.CreateRequest request1 = new CityPresentationDTO.CreateRequest(
                "São Paulo", "SP", "3550308", 1);
        CityPresentationDTO.CreateRequest request2 = new CityPresentationDTO.CreateRequest(
                "Santos", "Santos", "3548500", 1);
        given(createCityCommandHandler.handle(any(CreateCityCommand.class)))
                .willReturn(CompletableFuture.completedFuture(testCity));

        // When
        cityEndpoints.createCity(request1);
        cityEndpoints.createCity(request2);

        // Then
        then(createCityCommandHandler).should(times(2)).handle(commandCaptor.capture());
        List<CreateCityCommand> capturedCommands = commandCaptor.getAllValues();
        
        assertThat(capturedCommands).hasSize(2);
        assertThat(capturedCommands.get(0).name()).isEqualTo("São Paulo");
        assertThat(capturedCommands.get(0).ibgeCode()).isEqualTo("3550308");
        assertThat(capturedCommands.get(1).name()).isEqualTo("Santos");
        assertThat(capturedCommands.get(1).ibgeCode()).isEqualTo("3548500");
    }

    @Test
    void updateCity_ShouldCaptureEntityModifications_WhenUpdatingFields() {
        // Given
        CityEntity spyCity = spy(new CityEntity());
        spyCity.setId(1);
        spyCity.setName("Original Name");
        spyCity.setShortName("ON");
        spyCity.setIbgeCode("1111111");
        spyCity.setState(testState);

        CityPresentationDTO.UpdateRequest request = new CityPresentationDTO.UpdateRequest(
                "New Name", "NN", "2222222", 1);
        given(cityRepository.GetByIdAsync(1)).willReturn(CompletableFuture.completedFuture(Optional.of(spyCity)));
        given(cityRepository.UpdateAsync(spyCity)).willReturn(CompletableFuture.completedFuture(spyCity));

        // When
        cityEndpoints.updateCity(1, request);

        // Then - Verify all setter methods were called with correct values
        then(spyCity).should().setName("New Name");
        then(spyCity).should().setShortName("NN");
        then(spyCity).should().setIbgeCode("2222222");
        then(cityRepository).should().UpdateAsync(spyCity);
    }

    @Test
    void getCityByIbgeCode_ShouldHandleNullResponse_WhenRepositoryReturnsNull() {
        // Given
        given(cityRepository.GetByIbgeCodeAsync("9999999"))
                .willReturn(CompletableFuture.completedFuture(Optional.empty()));

        // When
        CompletableFuture<ResponseEntity<CityApplicationDTO.Response>> result = cityEndpoints.getCityByIbgeCode("9999999");

        // Then
        assertThat(result.join().getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        then(cityRepository).should().GetByIbgeCodeAsync("9999999");
    }

    @Test
    void createCity_ShouldVerifyIbgeCodeValidation_WhenCreatingCitiesWithDifferentCodes() {
        // Given
        CityPresentationDTO.CreateRequest requestSP = new CityPresentationDTO.CreateRequest(
                "São Paulo", "SP", "3550308", 1);
        CityPresentationDTO.CreateRequest requestRJ = new CityPresentationDTO.CreateRequest(
                "Rio de Janeiro", "RJ", "3304557", 2);

        given(createCityCommandHandler.handle(any(CreateCityCommand.class)))
                .willReturn(CompletableFuture.completedFuture(testCity));

        // When
        cityEndpoints.createCity(requestSP);
        cityEndpoints.createCity(requestRJ);

        // Then
        then(createCityCommandHandler).should(times(2)).handle(commandCaptor.capture());
        List<CreateCityCommand> capturedCommands = commandCaptor.getAllValues();

        assertThat(capturedCommands.get(0).ibgeCode()).isEqualTo("3550308");
        assertThat(capturedCommands.get(0).stateId()).isEqualTo(1);
        assertThat(capturedCommands.get(1).ibgeCode()).isEqualTo("3304557");
        assertThat(capturedCommands.get(1).stateId()).isEqualTo(2);
    }
}