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
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

/**
 * Advanced Mockito test class for AddressEndpoints demonstrating:
 * - ArgumentCaptor usage
 * - Spy objects
 * - Verification with times() and never()
 * - Custom argument matchers
 * - Exception handling in async contexts
 * - InOrder verification
 * - BDD style testing
 */
@ExtendWith(MockitoExtension.class)
class AddressEndpointsAdvancedMockitoTest {

    @Mock
    private IAddressRepository addressRepository;

    @Mock
    private CreateAddressCommandHandler createAddressCommandHandler;

    @InjectMocks
    private AddressEndpoints addressEndpoints;

    @Captor
    private ArgumentCaptor<CreateAddressCommand> commandCaptor;

    private AddressEntity testAddress;
    private CityEntity testCity;

    @BeforeEach
    void setUp() {
        CountryEntity testCountry = new CountryEntity();
        testCountry.setId(1);
        testCountry.setName("Brazil");

        StateEntity testState = new StateEntity();
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

        testAddress = new AddressEntity();
        testAddress.setId(1);
        testAddress.setStreet("Rua da Consolação");
        testAddress.setNumber("100");
        testAddress.setComplement("Apto 101");
        testAddress.setNeighborhood("Consolação");
        testAddress.setCity(testCity);
    }

    @Test
    void createAddress_ShouldCaptureCorrectCommand_WhenCalledWithValidRequest() {
        // Given
        AddressPresentationDTO.CreateRequest request = new AddressPresentationDTO.CreateRequest(
                "Rua Augusta", "123", "Cobertura", "Centro", 1);
        given(createAddressCommandHandler.handle(any(CreateAddressCommand.class)))
                .willReturn(CompletableFuture.completedFuture(testAddress));

        // When
        addressEndpoints.createAddress(request);

        // Then
        then(createAddressCommandHandler).should().handle(commandCaptor.capture());
        CreateAddressCommand capturedCommand = commandCaptor.getValue();
        
        assertThat(capturedCommand).isNotNull();
        assertThat(capturedCommand.street()).isEqualTo("Rua Augusta");
        assertThat(capturedCommand.number()).isEqualTo("123");
        assertThat(capturedCommand.complement()).isEqualTo("Cobertura");
        assertThat(capturedCommand.neighborhood()).isEqualTo("Centro");
        assertThat(capturedCommand.cityId()).isEqualTo(1);
    }

    @Test
    void createAddress_ShouldCallHandlerExactlyOnce_WhenCalledMultipleTimes() {
        // Given
        AddressPresentationDTO.CreateRequest request = new AddressPresentationDTO.CreateRequest(
                "Rua da Consolação", "100", "Apto 101", "Consolação", 1);
        given(createAddressCommandHandler.handle(any(CreateAddressCommand.class)))
                .willReturn(CompletableFuture.completedFuture(testAddress));

        // When
        addressEndpoints.createAddress(request);
        addressEndpoints.createAddress(request);

        // Then
        then(createAddressCommandHandler).should(times(2)).handle(any(CreateAddressCommand.class));
    }

    @Test
    void getAddresses_ShouldNeverCallRepository_WhenNotInvoked() {
        // When - Don't call getAddresses()
        
        // Then
        then(addressRepository).should(never()).GetAllAsync();
    }

    @Test
    void getAddressById_ShouldUseCustomArgumentMatcher_WhenCalledWithPositiveId() {
        // Given
        given(addressRepository.GetByIdAsync(argThat(id -> id > 0)))
                .willReturn(CompletableFuture.completedFuture(Optional.of(testAddress)));

        // When
        CompletableFuture<ResponseEntity<AddressApplicationDTO.Response>> result = addressEndpoints.getAddressById(5);

        // Then
        assertThat(result.join().getStatusCode()).isEqualTo(HttpStatus.OK);
        then(addressRepository).should().GetByIdAsync(argThat(id -> id > 0));
    }

    @Test
    void getAddresses_ShouldHandleEmptyList_UsingSpyList() {
        // Given - Using a spy to track interactions with the list
        List<Optional<AddressEntity>> emptyList = spy(new ArrayList<>());
        given(addressRepository.GetAllAsync()).willReturn(CompletableFuture.completedFuture(emptyList));

        // When
        CompletableFuture<ResponseEntity<List<AddressApplicationDTO.Response>>> result = addressEndpoints.getAddresses();

        // Then
        ResponseEntity<List<AddressApplicationDTO.Response>> response = result.join();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();
        
        // Verify the spy list was accessed
        verify(emptyList).stream();
    }

    @Test
    void updateAddress_ShouldCallRepositoryInCorrectOrder_WhenUpdatingAddress() {
        // Given
        AddressPresentationDTO.UpdateRequest request = new AddressPresentationDTO.UpdateRequest(
                "Rua da Consolação Updated", "200", "Apto 202", "Centro", 1);
        AddressEntity updatedAddress = new AddressEntity();
        updatedAddress.setId(1);
        updatedAddress.setStreet("Rua da Consolação Updated");
        updatedAddress.setNumber("200");
        updatedAddress.setCity(testCity);

        given(addressRepository.GetByIdAsync(1)).willReturn(CompletableFuture.completedFuture(Optional.of(testAddress)));
        given(addressRepository.UpdateAsync(testAddress)).willReturn(CompletableFuture.completedFuture(updatedAddress));

        // When
        addressEndpoints.updateAddress(1, request);

        // Then - Verify method call order
        InOrder inOrder = inOrder(addressRepository);
        inOrder.verify(addressRepository).GetByIdAsync(1);
        inOrder.verify(addressRepository).UpdateAsync(testAddress);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    void deleteAddress_ShouldVerifyExactArguments_WhenCalledWithSpecificId() {
        // Given
        given(addressRepository.DeleteAsync(eq(42))).willReturn(CompletableFuture.completedFuture(testAddress));

        // When
        addressEndpoints.deleteAddress(42);

        // Then
        then(addressRepository).should().DeleteAsync(eq(42));
        then(addressRepository).should(never()).DeleteAsync(intThat(id -> id != 42));
    }

    @Test
    void createAddress_ShouldVerifyNoInteractionWithRepository_WhenHandlerIsUsed() {
        // Given
        AddressPresentationDTO.CreateRequest request = new AddressPresentationDTO.CreateRequest(
                "Rua Augusta", "123", "Cobertura", "Centro", 1);
        given(createAddressCommandHandler.handle(any(CreateAddressCommand.class)))
                .willReturn(CompletableFuture.completedFuture(testAddress));

        // When
        addressEndpoints.createAddress(request);

        // Then - Verify no interaction with repository (only with command handler)
        then(addressRepository).shouldHaveNoInteractions();
        then(createAddressCommandHandler).should().handle(any(CreateAddressCommand.class));
    }

    @Test
    void createAddress_ShouldVerifyCommandFields_UsingMultipleCaptures() {
        // Given
        AddressPresentationDTO.CreateRequest request1 = new AddressPresentationDTO.CreateRequest(
                "Rua da Consolação", "100", "Apto 101", "Consolação", 1);
        AddressPresentationDTO.CreateRequest request2 = new AddressPresentationDTO.CreateRequest(
                "Avenida Paulista", "1000", "Sala 200", "Bela Vista", 1);
        given(createAddressCommandHandler.handle(any(CreateAddressCommand.class)))
                .willReturn(CompletableFuture.completedFuture(testAddress));

        // When
        addressEndpoints.createAddress(request1);
        addressEndpoints.createAddress(request2);

        // Then
        then(createAddressCommandHandler).should(times(2)).handle(commandCaptor.capture());
        List<CreateAddressCommand> capturedCommands = commandCaptor.getAllValues();
        
        assertThat(capturedCommands).hasSize(2);
        assertThat(capturedCommands.get(0).street()).isEqualTo("Rua da Consolação");
        assertThat(capturedCommands.get(0).neighborhood()).isEqualTo("Consolação");
        assertThat(capturedCommands.get(1).street()).isEqualTo("Avenida Paulista");
        assertThat(capturedCommands.get(1).neighborhood()).isEqualTo("Bela Vista");
    }

    @Test
    void updateAddress_ShouldCaptureEntityModifications_WhenUpdatingFields() {
        // Given
        AddressEntity spyAddress = spy(new AddressEntity());
        spyAddress.setId(1);
        spyAddress.setStreet("Original Street");
        spyAddress.setNumber("1");
        spyAddress.setComplement("Original Complement");
        spyAddress.setNeighborhood("Original Neighborhood");
        spyAddress.setCity(testCity);

        AddressPresentationDTO.UpdateRequest request = new AddressPresentationDTO.UpdateRequest(
                "New Street", "2", "New Complement", "New Neighborhood", 1);
        given(addressRepository.GetByIdAsync(1)).willReturn(CompletableFuture.completedFuture(Optional.of(spyAddress)));
        given(addressRepository.UpdateAsync(spyAddress)).willReturn(CompletableFuture.completedFuture(spyAddress));

        // When
        addressEndpoints.updateAddress(1, request);

        // Then - Verify all setter methods were called with correct values
        then(spyAddress).should().setStreet("New Street");
        then(spyAddress).should().setNumber("2");
        then(spyAddress).should().setComplement("New Complement");
        then(spyAddress).should().setNeighborhood("New Neighborhood");
        then(addressRepository).should().UpdateAsync(spyAddress);
    }
}