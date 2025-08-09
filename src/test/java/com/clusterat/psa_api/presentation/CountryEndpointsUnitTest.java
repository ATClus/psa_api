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
class CountryEndpointsUnitTest {

    @Mock
    private ICountryRepository countryRepository;

    @Mock
    private CreateCountryCommandHandler createCountryCommandHandler;

    @InjectMocks
    private CountryEndpoints countryEndpoints;

    private CountryEntity testCountry;
    private List<Optional<CountryEntity>> testCountries;

    @BeforeEach
    void setUp() {
        testCountry = spy(new CountryEntity());
        testCountry.setId(1);
        testCountry.setName("Brazil");
        testCountry.setShortName("BR");
        testCountry.setIsoCode("BRA");

        CountryEntity testCountry2 = spy(new CountryEntity());
        testCountry2.setId(2);
        testCountry2.setName("United States");
        testCountry2.setShortName("US");
        testCountry2.setIsoCode("USA");

        testCountries = Arrays.asList(Optional.of(testCountry), Optional.of(testCountry2));
    }

    @Test
    void getCountries_ShouldReturnListOfCountries_WhenCountriesExist() {
        // Given
        given(countryRepository.GetAllAsync()).willReturn(CompletableFuture.completedFuture(testCountries));

        // When
        CompletableFuture<ResponseEntity<List<CountryApplicationDTO.Response>>> result = countryEndpoints.getCountries();

        // Then
        assertThat(result).isCompleted();
        ResponseEntity<List<CountryApplicationDTO.Response>> response = result.join();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2);
        assertThat(response.getBody().get(0).id()).isEqualTo(1);
        assertThat(response.getBody().get(0).name()).isEqualTo("Brazil");
        assertThat(response.getBody().get(0).shortName()).isEqualTo("BR");
        assertThat(response.getBody().get(0).isoCode()).isEqualTo("BRA");

        then(countryRepository).should().GetAllAsync();
    }

    @Test
    void getCountries_ShouldReturnInternalServerError_WhenRepositoryThrowsException() {
        // Given
        given(countryRepository.GetAllAsync()).willReturn(CompletableFuture.failedFuture(new RuntimeException("Database error")));

        // When
        CompletableFuture<ResponseEntity<List<CountryApplicationDTO.Response>>> result = countryEndpoints.getCountries();

        // Then
        assertThat(result).isCompleted();
        ResponseEntity<List<CountryApplicationDTO.Response>> response = result.join();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNull();

        then(countryRepository).should().GetAllAsync();
    }

    @Test
    void getCountryById_ShouldReturnCountry_WhenCountryExists() {
        // Given
        given(countryRepository.GetByIdAsync(1)).willReturn(CompletableFuture.completedFuture(Optional.of(testCountry)));

        // When
        CompletableFuture<ResponseEntity<CountryApplicationDTO.Response>> result = countryEndpoints.getCountryById(1);

        // Then
        assertThat(result).isCompleted();
        ResponseEntity<CountryApplicationDTO.Response> response = result.join();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isEqualTo(1);
        assertThat(response.getBody().name()).isEqualTo("Brazil");
        assertThat(response.getBody().shortName()).isEqualTo("BR");
        assertThat(response.getBody().isoCode()).isEqualTo("BRA");

        then(countryRepository).should().GetByIdAsync(1);
    }

    @Test
    void getCountryById_ShouldReturnNotFound_WhenCountryDoesNotExist() {
        // Given
        given(countryRepository.GetByIdAsync(999)).willReturn(CompletableFuture.completedFuture(Optional.empty()));

        // When
        CompletableFuture<ResponseEntity<CountryApplicationDTO.Response>> result = countryEndpoints.getCountryById(999);

        // Then
        assertThat(result).isCompleted();
        ResponseEntity<CountryApplicationDTO.Response> response = result.join();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNull();

        then(countryRepository).should().GetByIdAsync(999);
    }

    @Test
    void getCountryById_ShouldReturnInternalServerError_WhenRepositoryThrowsException() {
        // Given
        given(countryRepository.GetByIdAsync(1)).willReturn(CompletableFuture.failedFuture(new RuntimeException("Database error")));

        // When
        CompletableFuture<ResponseEntity<CountryApplicationDTO.Response>> result = countryEndpoints.getCountryById(1);

        // Then
        assertThat(result).isCompleted();
        ResponseEntity<CountryApplicationDTO.Response> response = result.join();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNull();

        then(countryRepository).should().GetByIdAsync(1);
    }

    @Test
    void getCountryByIsoCode_ShouldReturnCountry_WhenCountryExists() {
        // Given
        given(countryRepository.GetByIsoCodeAsync("BRA")).willReturn(CompletableFuture.completedFuture(Optional.of(testCountry)));

        // When
        CompletableFuture<ResponseEntity<CountryApplicationDTO.Response>> result = countryEndpoints.getCountryByIsoCode("BRA");

        // Then
        assertThat(result).isCompleted();
        ResponseEntity<CountryApplicationDTO.Response> response = result.join();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isoCode()).isEqualTo("BRA");
        assertThat(response.getBody().name()).isEqualTo("Brazil");

        then(countryRepository).should().GetByIsoCodeAsync("BRA");
    }

    @Test
    void getCountryByIsoCode_ShouldReturnNotFound_WhenCountryDoesNotExist() {
        // Given
        given(countryRepository.GetByIsoCodeAsync("XYZ")).willReturn(CompletableFuture.completedFuture(Optional.empty()));

        // When
        CompletableFuture<ResponseEntity<CountryApplicationDTO.Response>> result = countryEndpoints.getCountryByIsoCode("XYZ");

        // Then
        assertThat(result).isCompleted();
        ResponseEntity<CountryApplicationDTO.Response> response = result.join();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNull();

        then(countryRepository).should().GetByIsoCodeAsync("XYZ");
    }

    @Test
    void createCountry_ShouldCreateAndReturnCountry_WhenValidRequest() {
        // Given
        CountryPresentationDTO.CreateRequest request = new CountryPresentationDTO.CreateRequest("Argentina", "AR", "ARG");
        given(createCountryCommandHandler.handle(any(CreateCountryCommand.class))).willReturn(CompletableFuture.completedFuture(testCountry));

        // When
        CompletableFuture<ResponseEntity<CountryApplicationDTO.Response>> result = countryEndpoints.createCountry(request);

        // Then
        assertThat(result).isCompleted();
        ResponseEntity<CountryApplicationDTO.Response> response = result.join();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().name()).isEqualTo("Brazil");
        assertThat(response.getBody().isoCode()).isEqualTo("BRA");

        then(createCountryCommandHandler).should().handle(argThat(command -> 
            command.name().equals("Argentina") &&
            command.shortName().equals("AR") &&
            command.isoCode().equals("ARG")
        ));
    }

    @Test
    void createCountry_ShouldReturnBadRequest_WhenCommandHandlerThrowsException() {
        // Given
        CountryPresentationDTO.CreateRequest request = new CountryPresentationDTO.CreateRequest("Invalid", "", "");
        given(createCountryCommandHandler.handle(any(CreateCountryCommand.class)))
                .willReturn(CompletableFuture.failedFuture(new IllegalArgumentException("Invalid country data")));

        // When
        CompletableFuture<ResponseEntity<CountryApplicationDTO.Response>> result = countryEndpoints.createCountry(request);

        // Then
        assertThat(result).isCompleted();
        ResponseEntity<CountryApplicationDTO.Response> response = result.join();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNull();

        then(createCountryCommandHandler).should().handle(any(CreateCountryCommand.class));
    }

    @Test
    void updateCountry_ShouldUpdateAndReturnCountry_WhenCountryExists() {
        // Given
        CountryPresentationDTO.UpdateRequest request = new CountryPresentationDTO.UpdateRequest("Brazil Updated", "BRZ", "BRZU");
        CountryEntity updatedCountry = spy(new CountryEntity());
        updatedCountry.setId(1);
        updatedCountry.setName("Brazil Updated");
        updatedCountry.setShortName("BRZ");
        updatedCountry.setIsoCode("BRZU");
        
        given(countryRepository.GetByIdAsync(1)).willReturn(CompletableFuture.completedFuture(Optional.of(testCountry)));
        given(countryRepository.UpdateAsync(testCountry)).willReturn(CompletableFuture.completedFuture(updatedCountry));

        // When
        CompletableFuture<ResponseEntity<CountryApplicationDTO.Response>> result = countryEndpoints.updateCountry(1, request);

        // Then
        assertThat(result).isCompleted();
        ResponseEntity<CountryApplicationDTO.Response> response = result.join();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().name()).isEqualTo("Brazil Updated");
        assertThat(response.getBody().shortName()).isEqualTo("BRZ");
        assertThat(response.getBody().isoCode()).isEqualTo("BRZU");

        then(countryRepository).should().GetByIdAsync(1);
        then(testCountry).should().setName("Brazil Updated");
        then(testCountry).should().setShortName("BRZ");
        then(testCountry).should().setIsoCode("BRZU");
        then(countryRepository).should().UpdateAsync(testCountry);
    }

    @Test
    void updateCountry_ShouldReturnNotFound_WhenCountryDoesNotExist() {
        // Given
        CountryPresentationDTO.UpdateRequest request = new CountryPresentationDTO.UpdateRequest("Non-existent", "NE", "NEX");
        given(countryRepository.GetByIdAsync(999)).willReturn(CompletableFuture.completedFuture(Optional.empty()));

        // When
        CompletableFuture<ResponseEntity<CountryApplicationDTO.Response>> result = countryEndpoints.updateCountry(999, request);

        // Then
        assertThat(result).isCompleted();
        ResponseEntity<CountryApplicationDTO.Response> response = result.join();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNull();

        then(countryRepository).should().GetByIdAsync(999);
        then(countryRepository).should(never()).UpdateAsync(any());
    }

    @Test
    void updateCountry_ShouldReturnInternalServerError_WhenUpdateFails() {
        // Given
        CountryPresentationDTO.UpdateRequest request = new CountryPresentationDTO.UpdateRequest("Brazil Updated", "BRZ", "BRZU");
        given(countryRepository.GetByIdAsync(1)).willReturn(CompletableFuture.completedFuture(Optional.of(testCountry)));
        given(countryRepository.UpdateAsync(testCountry)).willReturn(CompletableFuture.failedFuture(new RuntimeException("Update failed")));

        // When
        CompletableFuture<ResponseEntity<CountryApplicationDTO.Response>> result = countryEndpoints.updateCountry(1, request);

        // Then
        assertThat(result).isCompleted();
        ResponseEntity<CountryApplicationDTO.Response> response = result.join();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNull();

        then(countryRepository).should().GetByIdAsync(1);
        then(countryRepository).should().UpdateAsync(testCountry);
    }

    @Test
    void deleteCountry_ShouldReturnNoContent_WhenCountryDeleted() {
        // Given
        given(countryRepository.DeleteAsync(1)).willReturn(CompletableFuture.completedFuture(testCountry));

        // When
        CompletableFuture<ResponseEntity<Void>> result = countryEndpoints.deleteCountry(1);

        // Then
        assertThat(result).isCompleted();
        ResponseEntity<Void> response = result.join();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();

        then(countryRepository).should().DeleteAsync(1);
    }

    @Test
    void deleteCountry_ShouldReturnNotFound_WhenDeleteFails() {
        // Given
        given(countryRepository.DeleteAsync(999)).willReturn(CompletableFuture.failedFuture(new RuntimeException("Country not found")));

        // When
        CompletableFuture<ResponseEntity<Void>> result = countryEndpoints.deleteCountry(999);

        // Then
        assertThat(result).isCompleted();
        ResponseEntity<Void> response = result.join();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNull();

        then(countryRepository).should().DeleteAsync(999);
    }
}