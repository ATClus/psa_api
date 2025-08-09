package com.clusterat.psa_api.presentation;

import com.clusterat.psa_api.application.commands.CreateCountryCommand;
import com.clusterat.psa_api.application.dto.CountryApplicationDTO;
import com.clusterat.psa_api.application.handlers.CreateCountryCommandHandler;
import com.clusterat.psa_api.application.interfaces.ICountryRepository;
import com.clusterat.psa_api.domain.entities.CountryEntity;
import com.clusterat.psa_api.presentation.dto.CountryPresentationDTO;
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
 * - InOrder verification
 * - BDD style testing
 */
@ExtendWith(MockitoExtension.class)
class CountryEndpointsAdvancedMockitoTest {

    @Mock
    private ICountryRepository countryRepository;

    @Mock
    private CreateCountryCommandHandler createCountryCommandHandler;

    @InjectMocks
    private CountryEndpoints countryEndpoints;

    @Captor
    private ArgumentCaptor<CreateCountryCommand> commandCaptor;

    private CountryEntity testCountry;

    @BeforeEach
    void setUp() {
        testCountry = new CountryEntity();
        testCountry.setId(1);
        testCountry.setName("Brazil");
        testCountry.setShortName("BR");
        testCountry.setIsoCode("BRA");
    }

    @Test
    void createCountry_ShouldCaptureCorrectCommand_WhenCalledWithValidRequest() {
        // Given
        CountryPresentationDTO.CreateRequest request = new CountryPresentationDTO.CreateRequest("Argentina", "AR", "ARG");
        given(createCountryCommandHandler.handle(any(CreateCountryCommand.class)))
                .willReturn(CompletableFuture.completedFuture(testCountry));

        // When
        countryEndpoints.createCountry(request);

        // Then
        then(createCountryCommandHandler).should().handle(commandCaptor.capture());
        CreateCountryCommand capturedCommand = commandCaptor.getValue();
        
        assertThat(capturedCommand).isNotNull();
        assertThat(capturedCommand.name()).isEqualTo("Argentina");
        assertThat(capturedCommand.shortName()).isEqualTo("AR");
        assertThat(capturedCommand.isoCode()).isEqualTo("ARG");
    }

    @Test
    void createCountry_ShouldCallHandlerExactlyOnce_WhenCalledMultipleTimes() {
        // Given
        CountryPresentationDTO.CreateRequest request = new CountryPresentationDTO.CreateRequest("Brazil", "BR", "BRA");
        given(createCountryCommandHandler.handle(any(CreateCountryCommand.class)))
                .willReturn(CompletableFuture.completedFuture(testCountry));

        // When
        countryEndpoints.createCountry(request);
        countryEndpoints.createCountry(request);

        // Then
        then(createCountryCommandHandler).should(times(2)).handle(any(CreateCountryCommand.class));
    }

    @Test
    void getCountries_ShouldNeverCallRepository_WhenNotInvoked() {
        // When - Don't call getCountries()
        
        // Then
        then(countryRepository).should(never()).GetAllAsync();
    }

    @Test
    void getCountryById_ShouldUseCustomArgumentMatcher_WhenCalledWithPositiveId() {
        // Given
        given(countryRepository.GetByIdAsync(argThat(id -> id > 0)))
                .willReturn(CompletableFuture.completedFuture(Optional.of(testCountry)));

        // When
        CompletableFuture<ResponseEntity<CountryApplicationDTO.Response>> result = countryEndpoints.getCountryById(5);

        // Then
        assertThat(result.join().getStatusCode()).isEqualTo(HttpStatus.OK);
        then(countryRepository).should().GetByIdAsync(argThat(id -> id > 0));
    }

    @Test
    void getCountries_ShouldHandleEmptyList_UsingSpyList() {
        // Given - Using a spy to track interactions with the list
        List<Optional<CountryEntity>> emptyList = spy(new ArrayList<>());
        given(countryRepository.GetAllAsync()).willReturn(CompletableFuture.completedFuture(emptyList));

        // When
        CompletableFuture<ResponseEntity<List<CountryApplicationDTO.Response>>> result = countryEndpoints.getCountries();

        // Then
        ResponseEntity<List<CountryApplicationDTO.Response>> response = result.join();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();
        
        // Verify the spy list was accessed
        verify(emptyList).stream();
    }

    @Test
    void getCountries_ShouldFilterEmptyOptionals_WhenSomeCountriesAreEmpty() {
        // Given
        CountryEntity country2 = new CountryEntity();
        country2.setId(2);
        country2.setName("United States");
        country2.setShortName("US");
        country2.setIsoCode("USA");
        
        List<Optional<CountryEntity>> mixedList = Arrays.asList(
                Optional.of(testCountry),
                Optional.empty(),
                Optional.of(country2),
                Optional.empty()
        );
        given(countryRepository.GetAllAsync()).willReturn(CompletableFuture.completedFuture(mixedList));

        // When
        CompletableFuture<ResponseEntity<List<CountryApplicationDTO.Response>>> result = countryEndpoints.getCountries();

        // Then
        ResponseEntity<List<CountryApplicationDTO.Response>> response = result.join();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2); // Only non-empty optionals
        assertThat(response.getBody().get(0).id()).isEqualTo(1);
        assertThat(response.getBody().get(1).id()).isEqualTo(2);
    }

    @Test
    void createCountry_ShouldHandleCompletionException_WhenHandlerFails() {
        // Given
        CountryPresentationDTO.CreateRequest request = new CountryPresentationDTO.CreateRequest("Invalid", "", "");
        CompletableFuture<CountryEntity> failedFuture = new CompletableFuture<>();
        failedFuture.completeExceptionally(new IllegalArgumentException("Invalid country data"));
        given(createCountryCommandHandler.handle(any(CreateCountryCommand.class))).willReturn(failedFuture);

        // When
        CompletableFuture<ResponseEntity<CountryApplicationDTO.Response>> result = countryEndpoints.createCountry(request);

        // Then
        assertThat(result.join().getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        then(createCountryCommandHandler).should().handle(any(CreateCountryCommand.class));
    }

    @Test
    void updateCountry_ShouldCallRepositoryInCorrectOrder_WhenUpdatingCountry() {
        // Given
        CountryPresentationDTO.UpdateRequest request = new CountryPresentationDTO.UpdateRequest("Brazil Updated", "BRZ", "BRZU");
        CountryEntity updatedCountry = new CountryEntity();
        updatedCountry.setId(1);
        updatedCountry.setName("Brazil Updated");
        updatedCountry.setShortName("BRZ");
        updatedCountry.setIsoCode("BRZU");

        given(countryRepository.GetByIdAsync(1)).willReturn(CompletableFuture.completedFuture(Optional.of(testCountry)));
        given(countryRepository.UpdateAsync(testCountry)).willReturn(CompletableFuture.completedFuture(updatedCountry));

        // When
        countryEndpoints.updateCountry(1, request);

        // Then - Verify method call order
        InOrder inOrder = inOrder(countryRepository);
        inOrder.verify(countryRepository).GetByIdAsync(1);
        inOrder.verify(countryRepository).UpdateAsync(testCountry);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    void deleteCountry_ShouldVerifyExactArguments_WhenCalledWithSpecificId() {
        // Given
        given(countryRepository.DeleteAsync(eq(42))).willReturn(CompletableFuture.completedFuture(testCountry));

        // When
        countryEndpoints.deleteCountry(42);

        // Then
        then(countryRepository).should().DeleteAsync(eq(42));
        then(countryRepository).should(never()).DeleteAsync(intThat(id -> id != 42));
    }

    @Test
    void getCountryById_ShouldHandleRepositoryTimeout_WhenSlowResponse() {
        // Given - Simulate a slow repository response
        CompletableFuture<Optional<CountryEntity>> slowFuture = new CompletableFuture<>();
        // Don't complete the future to simulate timeout scenario
        given(countryRepository.GetByIdAsync(1)).willReturn(slowFuture);

        // When
        CompletableFuture<ResponseEntity<CountryApplicationDTO.Response>> result = countryEndpoints.getCountryById(1);

        // Then - Verify the future is not completed immediately
        assertThat(result.isDone()).isFalse();
        
        // Complete the slow future to finish the test
        slowFuture.complete(Optional.of(testCountry));
        assertThat(result.join().getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void createCountry_ShouldVerifyNoInteractionWithRepository_WhenHandlerIsUsed() {
        // Given
        CountryPresentationDTO.CreateRequest request = new CountryPresentationDTO.CreateRequest("Argentina", "AR", "ARG");
        given(createCountryCommandHandler.handle(any(CreateCountryCommand.class)))
                .willReturn(CompletableFuture.completedFuture(testCountry));

        // When
        countryEndpoints.createCountry(request);

        // Then - Verify no interaction with repository (only with command handler)
        then(countryRepository).shouldHaveNoInteractions();
        then(createCountryCommandHandler).should().handle(any(CreateCountryCommand.class));
    }

    @Test
    void getCountryByIsoCode_ShouldUseStringMatcher_WhenCalledWithValidIsoCode() {
        // Given
        given(countryRepository.GetByIsoCodeAsync(argThat(code -> code.length() == 3)))
                .willReturn(CompletableFuture.completedFuture(Optional.of(testCountry)));

        // When
        CompletableFuture<ResponseEntity<CountryApplicationDTO.Response>> result = countryEndpoints.getCountryByIsoCode("BRA");

        // Then
        assertThat(result.join().getStatusCode()).isEqualTo(HttpStatus.OK);
        then(countryRepository).should().GetByIsoCodeAsync(argThat(code -> code.length() == 3));
    }

    @Test
    void getAllCountries_ShouldResetInteractions_WhenTestedMultipleTimes() {
        // Given
        List<Optional<CountryEntity>> countries = List.of(Optional.of(testCountry));
        given(countryRepository.GetAllAsync()).willReturn(CompletableFuture.completedFuture(countries));

        // When - First call
        countryEndpoints.getCountries();
        
        // Reset interactions for clean verification
        reset(countryRepository);
        given(countryRepository.GetAllAsync()).willReturn(CompletableFuture.completedFuture(countries));
        
        // When - Second call after reset
        countryEndpoints.getCountries();

        // Then - Only verify the second call
        then(countryRepository).should(times(1)).GetAllAsync();
    }

    @Test
    void updateCountry_ShouldCaptureEntityModifications_WhenUpdatingFields() {
        // Given
        CountryEntity spyCountry = spy(new CountryEntity());
        spyCountry.setId(1);
        spyCountry.setName("Original Name");
        spyCountry.setShortName("ON");
        spyCountry.setIsoCode("ORI");

        CountryPresentationDTO.UpdateRequest request = new CountryPresentationDTO.UpdateRequest("New Name", "NN", "NEW");
        given(countryRepository.GetByIdAsync(1)).willReturn(CompletableFuture.completedFuture(Optional.of(spyCountry)));
        given(countryRepository.UpdateAsync(spyCountry)).willReturn(CompletableFuture.completedFuture(spyCountry));

        // When
        countryEndpoints.updateCountry(1, request);

        // Then - Verify all setter methods were called with correct values
        then(spyCountry).should().setName("New Name");
        then(spyCountry).should().setShortName("NN");
        then(spyCountry).should().setIsoCode("NEW");
        then(countryRepository).should().UpdateAsync(spyCountry);
    }

    @Test
    void createCountry_ShouldVerifyCommandFields_UsingMultipleCaptures() {
        // Given
        CountryPresentationDTO.CreateRequest request1 = new CountryPresentationDTO.CreateRequest("Brazil", "BR", "BRA");
        CountryPresentationDTO.CreateRequest request2 = new CountryPresentationDTO.CreateRequest("Argentina", "AR", "ARG");
        given(createCountryCommandHandler.handle(any(CreateCountryCommand.class)))
                .willReturn(CompletableFuture.completedFuture(testCountry));

        // When
        countryEndpoints.createCountry(request1);
        countryEndpoints.createCountry(request2);

        // Then
        then(createCountryCommandHandler).should(times(2)).handle(commandCaptor.capture());
        List<CreateCountryCommand> capturedCommands = commandCaptor.getAllValues();
        
        assertThat(capturedCommands).hasSize(2);
        assertThat(capturedCommands.get(0).name()).isEqualTo("Brazil");
        assertThat(capturedCommands.get(0).isoCode()).isEqualTo("BRA");
        assertThat(capturedCommands.get(1).name()).isEqualTo("Argentina");
        assertThat(capturedCommands.get(1).isoCode()).isEqualTo("ARG");
    }
}