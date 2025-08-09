package com.clusterat.psa_api.presentation;

import com.clusterat.psa_api.application.commands.CreateAddressCommand;
import com.clusterat.psa_api.application.dto.AddressApplicationDTO;
import com.clusterat.psa_api.application.handlers.CreateAddressCommandHandler;
import com.clusterat.psa_api.application.interfaces.IAddressRepository;
import com.clusterat.psa_api.domain.entities.AddressEntity;
import com.clusterat.psa_api.domain.entities.CityEntity;
import com.clusterat.psa_api.domain.entities.CountryEntity;
import com.clusterat.psa_api.domain.entities.StateEntity;
import com.clusterat.psa_api.domain.value_objects.Region;
import com.clusterat.psa_api.presentation.dto.AddressPresentationDTO;
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
class AddressEndpointsUnitTest {

    @Mock
    private IAddressRepository addressRepository;

    @Mock
    private CreateAddressCommandHandler createAddressCommandHandler;

    @InjectMocks
    private AddressEndpoints addressEndpoints;

    private AddressEntity testAddress;
    private CityEntity testCity;
    private List<Optional<AddressEntity>> testAddresses;

    @BeforeEach
    void setUp() {
        CountryEntity testCountry = spy(new CountryEntity());
        testCountry.setId(1);
        testCountry.setName("Brazil");

        StateEntity testState = spy(new StateEntity());
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

        testAddress = spy(new AddressEntity());
        testAddress.setId(1);
        testAddress.setStreet("Rua da Consolação");
        testAddress.setNumber("100");
        testAddress.setComplement("Apto 101");
        testAddress.setNeighborhood("Consolação");
        testAddress.setCity(testCity);

        AddressEntity testAddress2 = spy(new AddressEntity());
        testAddress2.setId(2);
        testAddress2.setStreet("Avenida Paulista");
        testAddress2.setNumber("1000");
        testAddress2.setComplement("Sala 200");
        testAddress2.setNeighborhood("Bela Vista");
        testAddress2.setCity(testCity);

        testAddresses = Arrays.asList(Optional.of(testAddress), Optional.of(testAddress2));
    }

    @Test
    void getAddresses_ShouldReturnListOfAddresses_WhenAddressesExist() {
        // Given
        given(addressRepository.GetAllAsync()).willReturn(CompletableFuture.completedFuture(testAddresses));

        // When
        CompletableFuture<ResponseEntity<List<AddressApplicationDTO.Response>>> result = addressEndpoints.getAddresses();

        // Then
        assertThat(result).isCompleted();
        ResponseEntity<List<AddressApplicationDTO.Response>> response = result.join();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2);
        assertThat(response.getBody().get(0).id()).isEqualTo(1);
        assertThat(response.getBody().get(0).street()).isEqualTo("Rua da Consolação");
        assertThat(response.getBody().get(0).number()).isEqualTo("100");
        assertThat(response.getBody().get(0).complement()).isEqualTo("Apto 101");
        assertThat(response.getBody().get(0).neighborhood()).isEqualTo("Consolação");
        assertThat(response.getBody().get(0).cityId()).isEqualTo(1);

        then(addressRepository).should().GetAllAsync();
    }

    @Test
    void getAddresses_ShouldReturnInternalServerError_WhenRepositoryThrowsException() {
        // Given
        given(addressRepository.GetAllAsync()).willReturn(CompletableFuture.failedFuture(new RuntimeException("Database error")));

        // When
        CompletableFuture<ResponseEntity<List<AddressApplicationDTO.Response>>> result = addressEndpoints.getAddresses();

        // Then
        assertThat(result).isCompleted();
        ResponseEntity<List<AddressApplicationDTO.Response>> response = result.join();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNull();

        then(addressRepository).should().GetAllAsync();
    }

    @Test
    void getAddressById_ShouldReturnAddress_WhenAddressExists() {
        // Given
        given(addressRepository.GetByIdAsync(1)).willReturn(CompletableFuture.completedFuture(Optional.of(testAddress)));

        // When
        CompletableFuture<ResponseEntity<AddressApplicationDTO.Response>> result = addressEndpoints.getAddressById(1);

        // Then
        assertThat(result).isCompleted();
        ResponseEntity<AddressApplicationDTO.Response> response = result.join();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isEqualTo(1);
        assertThat(response.getBody().street()).isEqualTo("Rua da Consolação");
        assertThat(response.getBody().number()).isEqualTo("100");
        assertThat(response.getBody().complement()).isEqualTo("Apto 101");
        assertThat(response.getBody().neighborhood()).isEqualTo("Consolação");
        assertThat(response.getBody().cityId()).isEqualTo(1);

        then(addressRepository).should().GetByIdAsync(1);
    }

    @Test
    void getAddressById_ShouldReturnNotFound_WhenAddressDoesNotExist() {
        // Given
        given(addressRepository.GetByIdAsync(999)).willReturn(CompletableFuture.completedFuture(Optional.empty()));

        // When
        CompletableFuture<ResponseEntity<AddressApplicationDTO.Response>> result = addressEndpoints.getAddressById(999);

        // Then
        assertThat(result).isCompleted();
        ResponseEntity<AddressApplicationDTO.Response> response = result.join();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNull();

        then(addressRepository).should().GetByIdAsync(999);
    }

    @Test
    void getAddressById_ShouldReturnInternalServerError_WhenRepositoryThrowsException() {
        // Given
        given(addressRepository.GetByIdAsync(1)).willReturn(CompletableFuture.failedFuture(new RuntimeException("Database error")));

        // When
        CompletableFuture<ResponseEntity<AddressApplicationDTO.Response>> result = addressEndpoints.getAddressById(1);

        // Then
        assertThat(result).isCompleted();
        ResponseEntity<AddressApplicationDTO.Response> response = result.join();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNull();

        then(addressRepository).should().GetByIdAsync(1);
    }

    @Test
    void createAddress_ShouldCreateAndReturnAddress_WhenValidRequest() {
        // Given
        AddressPresentationDTO.CreateRequest request = new AddressPresentationDTO.CreateRequest(
                "Rua Augusta", "123", "Cobertura", "Centro", 1);
        given(createAddressCommandHandler.handle(any(CreateAddressCommand.class)))
                .willReturn(CompletableFuture.completedFuture(testAddress));

        // When
        CompletableFuture<ResponseEntity<AddressApplicationDTO.Response>> result = addressEndpoints.createAddress(request);

        // Then
        assertThat(result).isCompleted();
        ResponseEntity<AddressApplicationDTO.Response> response = result.join();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().street()).isEqualTo("Rua da Consolação");

        then(createAddressCommandHandler).should().handle(argThat(command -> 
            command.street().equals("Rua Augusta") &&
            command.number().equals("123") &&
            command.complement().equals("Cobertura") &&
            command.neighborhood().equals("Centro") &&
            command.cityId() == 1
        ));
    }

    @Test
    void createAddress_ShouldReturnBadRequest_WhenCommandHandlerThrowsException() {
        // Given
        AddressPresentationDTO.CreateRequest request = new AddressPresentationDTO.CreateRequest(
                "Invalid", "", "", "", 999);
        given(createAddressCommandHandler.handle(any(CreateAddressCommand.class)))
                .willReturn(CompletableFuture.failedFuture(new IllegalArgumentException("Invalid address data")));

        // When
        CompletableFuture<ResponseEntity<AddressApplicationDTO.Response>> result = addressEndpoints.createAddress(request);

        // Then
        assertThat(result).isCompleted();
        ResponseEntity<AddressApplicationDTO.Response> response = result.join();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNull();

        then(createAddressCommandHandler).should().handle(any(CreateAddressCommand.class));
    }

    @Test
    void updateAddress_ShouldUpdateAndReturnAddress_WhenAddressExists() {
        // Given
        AddressPresentationDTO.UpdateRequest request = new AddressPresentationDTO.UpdateRequest(
                "Rua da Consolação Updated", "200", "Apto 202", "Centro", 1);
        AddressEntity updatedAddress = spy(new AddressEntity());
        updatedAddress.setId(1);
        updatedAddress.setStreet("Rua da Consolação Updated");
        updatedAddress.setNumber("200");
        updatedAddress.setComplement("Apto 202");
        updatedAddress.setNeighborhood("Centro");
        updatedAddress.setCity(testCity);
        
        given(addressRepository.GetByIdAsync(1)).willReturn(CompletableFuture.completedFuture(Optional.of(testAddress)));
        given(addressRepository.UpdateAsync(testAddress)).willReturn(CompletableFuture.completedFuture(updatedAddress));

        // When
        CompletableFuture<ResponseEntity<AddressApplicationDTO.Response>> result = addressEndpoints.updateAddress(1, request);

        // Then
        assertThat(result).isCompleted();
        ResponseEntity<AddressApplicationDTO.Response> response = result.join();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().street()).isEqualTo("Rua da Consolação Updated");
        assertThat(response.getBody().number()).isEqualTo("200");
        assertThat(response.getBody().complement()).isEqualTo("Apto 202");
        assertThat(response.getBody().neighborhood()).isEqualTo("Centro");

        then(addressRepository).should().GetByIdAsync(1);
        then(testAddress).should().setStreet("Rua da Consolação Updated");
        then(testAddress).should().setNumber("200");
        then(testAddress).should().setComplement("Apto 202");
        then(testAddress).should().setNeighborhood("Centro");
        then(addressRepository).should().UpdateAsync(testAddress);
    }

    @Test
    void updateAddress_ShouldReturnNotFound_WhenAddressDoesNotExist() {
        // Given
        AddressPresentationDTO.UpdateRequest request = new AddressPresentationDTO.UpdateRequest(
                "Non-existent", "0", "", "", 1);
        given(addressRepository.GetByIdAsync(999)).willReturn(CompletableFuture.completedFuture(Optional.empty()));

        // When
        CompletableFuture<ResponseEntity<AddressApplicationDTO.Response>> result = addressEndpoints.updateAddress(999, request);

        // Then
        assertThat(result).isCompleted();
        ResponseEntity<AddressApplicationDTO.Response> response = result.join();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNull();

        then(addressRepository).should().GetByIdAsync(999);
        then(addressRepository).should(never()).UpdateAsync(any());
    }

    @Test
    void updateAddress_ShouldReturnInternalServerError_WhenUpdateFails() {
        // Given
        AddressPresentationDTO.UpdateRequest request = new AddressPresentationDTO.UpdateRequest(
                "Rua da Consolação Updated", "200", "Apto 202", "Centro", 1);
        given(addressRepository.GetByIdAsync(1)).willReturn(CompletableFuture.completedFuture(Optional.of(testAddress)));
        given(addressRepository.UpdateAsync(testAddress)).willReturn(CompletableFuture.failedFuture(new RuntimeException("Update failed")));

        // When
        CompletableFuture<ResponseEntity<AddressApplicationDTO.Response>> result = addressEndpoints.updateAddress(1, request);

        // Then
        assertThat(result).isCompleted();
        ResponseEntity<AddressApplicationDTO.Response> response = result.join();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNull();

        then(addressRepository).should().GetByIdAsync(1);
        then(addressRepository).should().UpdateAsync(testAddress);
    }

    @Test
    void deleteAddress_ShouldReturnNoContent_WhenAddressDeleted() {
        // Given
        given(addressRepository.DeleteAsync(1)).willReturn(CompletableFuture.completedFuture(testAddress));

        // When
        CompletableFuture<ResponseEntity<Void>> result = addressEndpoints.deleteAddress(1);

        // Then
        assertThat(result).isCompleted();
        ResponseEntity<Void> response = result.join();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();

        then(addressRepository).should().DeleteAsync(1);
    }

    @Test
    void deleteAddress_ShouldReturnNotFound_WhenDeleteFails() {
        // Given
        given(addressRepository.DeleteAsync(999)).willReturn(CompletableFuture.failedFuture(new RuntimeException("Address not found")));

        // When
        CompletableFuture<ResponseEntity<Void>> result = addressEndpoints.deleteAddress(999);

        // Then
        assertThat(result).isCompleted();
        ResponseEntity<Void> response = result.join();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNull();

        then(addressRepository).should().DeleteAsync(999);
    }
}