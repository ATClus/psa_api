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
class CityEndpointsUnitTest {

    @Mock
    private ICityRepository cityRepository;

    @Mock
    private CreateCityCommandHandler createCityCommandHandler;

    @InjectMocks
    private CityEndpoints cityEndpoints;

    private CityEntity testCity;
    private StateEntity testState;
    private List<Optional<CityEntity>> testCities;

    @BeforeEach
    void setUp() {
        CountryEntity testCountry = spy(new CountryEntity());
        testCountry.setId(1);
        testCountry.setName("Brazil");

        testState = spy(new StateEntity());
        testState.setId(1);
        testState.setName("São Paulo");
        testState.setShortName("SP");
        testState.setRegion(Region.SUDESTE);
        testState.setCountry(testCountry);

        testCity = spy(new CityEntity());
        testCity.setId(1);
        testCity.setName("São Paulo");
        testCity.setShortName("SP");
        testCity.setIbgeCode("3550308");
        testCity.setState(testState);

        CityEntity testCity2 = spy(new CityEntity());
        testCity2.setId(2);
        testCity2.setName("Santos");
        testCity2.setShortName("Santos");
        testCity2.setIbgeCode("3548500");
        testCity2.setState(testState);

        testCities = Arrays.asList(Optional.of(testCity), Optional.of(testCity2));
    }

    @Test
    void getCities_ShouldReturnListOfCities_WhenCitiesExist() {
        // Given
        given(cityRepository.GetAllAsync()).willReturn(CompletableFuture.completedFuture(testCities));

        // When
        CompletableFuture<ResponseEntity<List<CityApplicationDTO.Response>>> result = cityEndpoints.getCities();

        // Then
        assertThat(result).isCompleted();
        ResponseEntity<List<CityApplicationDTO.Response>> response = result.join();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2);
        assertThat(response.getBody().get(0).id()).isEqualTo(1);
        assertThat(response.getBody().get(0).name()).isEqualTo("São Paulo");
        assertThat(response.getBody().get(0).shortName()).isEqualTo("SP");
        assertThat(response.getBody().get(0).ibgeCode()).isEqualTo("3550308");
        assertThat(response.getBody().get(0).stateId()).isEqualTo(1);

        then(cityRepository).should().GetAllAsync();
    }

    @Test
    void getCities_ShouldReturnInternalServerError_WhenRepositoryThrowsException() {
        // Given
        given(cityRepository.GetAllAsync()).willReturn(CompletableFuture.failedFuture(new RuntimeException("Database error")));

        // When
        CompletableFuture<ResponseEntity<List<CityApplicationDTO.Response>>> result = cityEndpoints.getCities();

        // Then
        assertThat(result).isCompleted();
        ResponseEntity<List<CityApplicationDTO.Response>> response = result.join();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNull();

        then(cityRepository).should().GetAllAsync();
    }

    @Test
    void getCityById_ShouldReturnCity_WhenCityExists() {
        // Given
        given(cityRepository.GetByIdAsync(1)).willReturn(CompletableFuture.completedFuture(Optional.of(testCity)));

        // When
        CompletableFuture<ResponseEntity<CityApplicationDTO.Response>> result = cityEndpoints.getCityById(1);

        // Then
        assertThat(result).isCompleted();
        ResponseEntity<CityApplicationDTO.Response> response = result.join();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isEqualTo(1);
        assertThat(response.getBody().name()).isEqualTo("São Paulo");
        assertThat(response.getBody().shortName()).isEqualTo("SP");
        assertThat(response.getBody().ibgeCode()).isEqualTo("3550308");
        assertThat(response.getBody().stateId()).isEqualTo(1);

        then(cityRepository).should().GetByIdAsync(1);
    }

    @Test
    void getCityById_ShouldReturnNotFound_WhenCityDoesNotExist() {
        // Given
        given(cityRepository.GetByIdAsync(999)).willReturn(CompletableFuture.completedFuture(Optional.empty()));

        // When
        CompletableFuture<ResponseEntity<CityApplicationDTO.Response>> result = cityEndpoints.getCityById(999);

        // Then
        assertThat(result).isCompleted();
        ResponseEntity<CityApplicationDTO.Response> response = result.join();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNull();

        then(cityRepository).should().GetByIdAsync(999);
    }

    @Test
    void getCityById_ShouldReturnInternalServerError_WhenRepositoryThrowsException() {
        // Given
        given(cityRepository.GetByIdAsync(1)).willReturn(CompletableFuture.failedFuture(new RuntimeException("Database error")));

        // When
        CompletableFuture<ResponseEntity<CityApplicationDTO.Response>> result = cityEndpoints.getCityById(1);

        // Then
        assertThat(result).isCompleted();
        ResponseEntity<CityApplicationDTO.Response> response = result.join();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNull();

        then(cityRepository).should().GetByIdAsync(1);
    }

    @Test
    void getCityByIbgeCode_ShouldReturnCity_WhenCityExists() {
        // Given
        given(cityRepository.GetByIbgeCodeAsync("3550308")).willReturn(CompletableFuture.completedFuture(Optional.of(testCity)));

        // When
        CompletableFuture<ResponseEntity<CityApplicationDTO.Response>> result = cityEndpoints.getCityByIbgeCode("3550308");

        // Then
        assertThat(result).isCompleted();
        ResponseEntity<CityApplicationDTO.Response> response = result.join();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().ibgeCode()).isEqualTo("3550308");
        assertThat(response.getBody().name()).isEqualTo("São Paulo");

        then(cityRepository).should().GetByIbgeCodeAsync("3550308");
    }

    @Test
    void getCityByIbgeCode_ShouldReturnNotFound_WhenCityDoesNotExist() {
        // Given
        given(cityRepository.GetByIbgeCodeAsync("9999999")).willReturn(CompletableFuture.completedFuture(Optional.empty()));

        // When
        CompletableFuture<ResponseEntity<CityApplicationDTO.Response>> result = cityEndpoints.getCityByIbgeCode("9999999");

        // Then
        assertThat(result).isCompleted();
        ResponseEntity<CityApplicationDTO.Response> response = result.join();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNull();

        then(cityRepository).should().GetByIbgeCodeAsync("9999999");
    }

    @Test
    void createCity_ShouldCreateAndReturnCity_WhenValidRequest() {
        // Given
        CityPresentationDTO.CreateRequest request = new CityPresentationDTO.CreateRequest(
                "Campinas", "Campinas", "3509502", 1);
        given(createCityCommandHandler.handle(any(CreateCityCommand.class)))
                .willReturn(CompletableFuture.completedFuture(testCity));

        // When
        CompletableFuture<ResponseEntity<CityApplicationDTO.Response>> result = cityEndpoints.createCity(request);

        // Then
        assertThat(result).isCompleted();
        ResponseEntity<CityApplicationDTO.Response> response = result.join();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().name()).isEqualTo("São Paulo");

        then(createCityCommandHandler).should().handle(argThat(command -> 
            command.name().equals("Campinas") &&
            command.shortName().equals("Campinas") &&
            command.ibgeCode().equals("3509502") &&
            command.stateId() == 1
        ));
    }

    @Test
    void createCity_ShouldReturnBadRequest_WhenCommandHandlerThrowsException() {
        // Given
        CityPresentationDTO.CreateRequest request = new CityPresentationDTO.CreateRequest(
                "Invalid", "", "", 999);
        given(createCityCommandHandler.handle(any(CreateCityCommand.class)))
                .willReturn(CompletableFuture.failedFuture(new IllegalArgumentException("Invalid city data")));

        // When
        CompletableFuture<ResponseEntity<CityApplicationDTO.Response>> result = cityEndpoints.createCity(request);

        // Then
        assertThat(result).isCompleted();
        ResponseEntity<CityApplicationDTO.Response> response = result.join();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNull();

        then(createCityCommandHandler).should().handle(any(CreateCityCommand.class));
    }

    @Test
    void updateCity_ShouldUpdateAndReturnCity_WhenCityExists() {
        // Given
        CityPresentationDTO.UpdateRequest request = new CityPresentationDTO.UpdateRequest(
                "São Paulo Updated", "SPU", "3550308", 1);
        CityEntity updatedCity = spy(new CityEntity());
        updatedCity.setId(1);
        updatedCity.setName("São Paulo Updated");
        updatedCity.setShortName("SPU");
        updatedCity.setIbgeCode("3550308");
        updatedCity.setState(testState);
        
        given(cityRepository.GetByIdAsync(1)).willReturn(CompletableFuture.completedFuture(Optional.of(testCity)));
        given(cityRepository.UpdateAsync(testCity)).willReturn(CompletableFuture.completedFuture(updatedCity));

        // When
        CompletableFuture<ResponseEntity<CityApplicationDTO.Response>> result = cityEndpoints.updateCity(1, request);

        // Then
        assertThat(result).isCompleted();
        ResponseEntity<CityApplicationDTO.Response> response = result.join();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().name()).isEqualTo("São Paulo Updated");
        assertThat(response.getBody().shortName()).isEqualTo("SPU");

        then(cityRepository).should().GetByIdAsync(1);
        then(testCity).should().setName("São Paulo Updated");
        then(testCity).should().setShortName("SPU");
        then(testCity).should().setIbgeCode("3550308");
        then(cityRepository).should().UpdateAsync(testCity);
    }

    @Test
    void updateCity_ShouldReturnNotFound_WhenCityDoesNotExist() {
        // Given
        CityPresentationDTO.UpdateRequest request = new CityPresentationDTO.UpdateRequest(
                "Non-existent", "NE", "0000000", 1);
        given(cityRepository.GetByIdAsync(999)).willReturn(CompletableFuture.completedFuture(Optional.empty()));

        // When
        CompletableFuture<ResponseEntity<CityApplicationDTO.Response>> result = cityEndpoints.updateCity(999, request);

        // Then
        assertThat(result).isCompleted();
        ResponseEntity<CityApplicationDTO.Response> response = result.join();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNull();

        then(cityRepository).should().GetByIdAsync(999);
        then(cityRepository).should(never()).UpdateAsync(any());
    }

    @Test
    void updateCity_ShouldReturnInternalServerError_WhenUpdateFails() {
        // Given
        CityPresentationDTO.UpdateRequest request = new CityPresentationDTO.UpdateRequest(
                "São Paulo Updated", "SPU", "3550308", 1);
        given(cityRepository.GetByIdAsync(1)).willReturn(CompletableFuture.completedFuture(Optional.of(testCity)));
        given(cityRepository.UpdateAsync(testCity)).willReturn(CompletableFuture.failedFuture(new RuntimeException("Update failed")));

        // When
        CompletableFuture<ResponseEntity<CityApplicationDTO.Response>> result = cityEndpoints.updateCity(1, request);

        // Then
        assertThat(result).isCompleted();
        ResponseEntity<CityApplicationDTO.Response> response = result.join();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNull();

        then(cityRepository).should().GetByIdAsync(1);
        then(cityRepository).should().UpdateAsync(testCity);
    }

    @Test
    void deleteCity_ShouldReturnNoContent_WhenCityDeleted() {
        // Given
        given(cityRepository.DeleteAsync(1)).willReturn(CompletableFuture.completedFuture(testCity));

        // When
        CompletableFuture<ResponseEntity<Void>> result = cityEndpoints.deleteCity(1);

        // Then
        assertThat(result).isCompleted();
        ResponseEntity<Void> response = result.join();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();

        then(cityRepository).should().DeleteAsync(1);
    }

    @Test
    void deleteCity_ShouldReturnNotFound_WhenDeleteFails() {
        // Given
        given(cityRepository.DeleteAsync(999)).willReturn(CompletableFuture.failedFuture(new RuntimeException("City not found")));

        // When
        CompletableFuture<ResponseEntity<Void>> result = cityEndpoints.deleteCity(999);

        // Then
        assertThat(result).isCompleted();
        ResponseEntity<Void> response = result.join();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNull();

        then(cityRepository).should().DeleteAsync(999);
    }
}