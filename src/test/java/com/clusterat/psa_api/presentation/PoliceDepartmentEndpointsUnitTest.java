package com.clusterat.psa_api.presentation;

import com.clusterat.psa_api.application.commands.CreatePoliceDepartmentCommand;
import com.clusterat.psa_api.application.dto.PoliceDepartmentApplicationDTO;
import com.clusterat.psa_api.application.handlers.CreatePoliceDepartmentCommandHandler;
import com.clusterat.psa_api.application.interfaces.IPoliceDepartmentRepository;
import com.clusterat.psa_api.domain.entities.*;
import com.clusterat.psa_api.domain.value_objects.Region;
import com.clusterat.psa_api.presentation.dto.PoliceDepartmentPresentationDTO;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;

@ExtendWith(MockitoExtension.class)
class PoliceDepartmentEndpointsUnitTest {

    @Mock
    private IPoliceDepartmentRepository policeDepartmentRepository;

    @Mock
    private CreatePoliceDepartmentCommandHandler createPoliceDepartmentCommandHandler;

    @InjectMocks
    private PoliceDepartmentEndpoints policeDepartmentEndpoints;

    private PoliceDepartmentEntity testPoliceDepartment;
    private AddressEntity testAddress;
    private List<Optional<PoliceDepartmentEntity>> testPoliceDepartments;

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

        CityEntity testCity = spy(new CityEntity());
        testCity.setId(1);
        testCity.setName("São Paulo");
        testCity.setShortName("SP");
        testCity.setIbgeCode("3550308");
        testCity.setState(testState);

        testAddress = spy(new AddressEntity());
        testAddress.setId(1);
        testAddress.setStreet("Rua da Consolação");
        testAddress.setNumber("100");
        testAddress.setComplement("Delegacia Central");
        testAddress.setNeighborhood("Centro");
        testAddress.setCity(testCity);

        testPoliceDepartment = spy(new PoliceDepartmentEntity());
        testPoliceDepartment.setId(1);
        testPoliceDepartment.setOverpassId("way/123456789");
        testPoliceDepartment.setName("1ª Delegacia de Polícia");
        testPoliceDepartment.setShortName("1ª DP");
        testPoliceDepartment.setOperator("Polícia Civil");
        testPoliceDepartment.setOwnership("Governo do Estado");
        testPoliceDepartment.setPhone("+55 11 3333-4444");
        testPoliceDepartment.setEmail("dp001@policia.sp.gov.br");
        testPoliceDepartment.setLatitude("-23.5505");
        testPoliceDepartment.setLongitude("-46.6333");
        testPoliceDepartment.setAddress(testAddress);

        PoliceDepartmentEntity testPoliceDepartment2 = spy(new PoliceDepartmentEntity());
        testPoliceDepartment2.setId(2);
        testPoliceDepartment2.setOverpassId("way/987654321");
        testPoliceDepartment2.setName("2ª Delegacia de Polícia");
        testPoliceDepartment2.setShortName("2ª DP");
        testPoliceDepartment2.setOperator("Polícia Civil");
        testPoliceDepartment2.setOwnership("Governo do Estado");
        testPoliceDepartment2.setPhone("+55 11 5555-6666");
        testPoliceDepartment2.setEmail("dp002@policia.sp.gov.br");
        testPoliceDepartment2.setLatitude("-23.5500");
        testPoliceDepartment2.setLongitude("-46.6300");
        testPoliceDepartment2.setAddress(testAddress);

        testPoliceDepartments = Arrays.asList(Optional.of(testPoliceDepartment), Optional.of(testPoliceDepartment2));
    }

    @Test
    void getPoliceDepartments_ShouldReturnListOfPoliceDepartments_WhenPoliceDepartmentsExist() {
        // Given
        given(policeDepartmentRepository.GetAllAsync()).willReturn(CompletableFuture.completedFuture(testPoliceDepartments));

        // When
        CompletableFuture<ResponseEntity<List<PoliceDepartmentApplicationDTO.Response>>> result = policeDepartmentEndpoints.getPoliceDepartments();

        // Then
        assertThat(result).isCompleted();
        ResponseEntity<List<PoliceDepartmentApplicationDTO.Response>> response = result.join();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2);
        assertThat(response.getBody().get(0).id()).isEqualTo(1);
        assertThat(response.getBody().get(0).overpassId()).isEqualTo("way/123456789");
        assertThat(response.getBody().get(0).name()).isEqualTo("1ª Delegacia de Polícia");
        assertThat(response.getBody().get(0).shortName()).isEqualTo("1ª DP");
        assertThat(response.getBody().get(0).operator()).isEqualTo("Polícia Civil");
        assertThat(response.getBody().get(0).ownership()).isEqualTo("Governo do Estado");
        assertThat(response.getBody().get(0).phone()).isEqualTo("+55 11 3333-4444");
        assertThat(response.getBody().get(0).email()).isEqualTo("dp001@policia.sp.gov.br");
        assertThat(response.getBody().get(0).addressId()).isEqualTo(1);

        then(policeDepartmentRepository).should().GetAllAsync();
    }

    @Test
    void getPoliceDepartments_ShouldReturnInternalServerError_WhenRepositoryThrowsException() {
        // Given
        given(policeDepartmentRepository.GetAllAsync()).willReturn(CompletableFuture.failedFuture(new RuntimeException("Database error")));

        // When
        CompletableFuture<ResponseEntity<List<PoliceDepartmentApplicationDTO.Response>>> result = policeDepartmentEndpoints.getPoliceDepartments();

        // Then
        assertThat(result).isCompleted();
        ResponseEntity<List<PoliceDepartmentApplicationDTO.Response>> response = result.join();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNull();

        then(policeDepartmentRepository).should().GetAllAsync();
    }

    @Test
    void getPoliceDepartmentById_ShouldReturnPoliceDepartment_WhenPoliceDepartmentExists() {
        // Given
        given(policeDepartmentRepository.GetByIdAsync(1)).willReturn(CompletableFuture.completedFuture(Optional.of(testPoliceDepartment)));

        // When
        CompletableFuture<ResponseEntity<PoliceDepartmentApplicationDTO.Response>> result = policeDepartmentEndpoints.getPoliceDepartmentById(1);

        // Then
        assertThat(result).isCompleted();
        ResponseEntity<PoliceDepartmentApplicationDTO.Response> response = result.join();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isEqualTo(1);
        assertThat(response.getBody().overpassId()).isEqualTo("way/123456789");
        assertThat(response.getBody().name()).isEqualTo("1ª Delegacia de Polícia");
        assertThat(response.getBody().addressId()).isEqualTo(1);

        then(policeDepartmentRepository).should().GetByIdAsync(1);
    }

    @Test
    void getPoliceDepartmentById_ShouldReturnNotFound_WhenPoliceDepartmentDoesNotExist() {
        // Given
        given(policeDepartmentRepository.GetByIdAsync(999)).willReturn(CompletableFuture.completedFuture(Optional.empty()));

        // When
        CompletableFuture<ResponseEntity<PoliceDepartmentApplicationDTO.Response>> result = policeDepartmentEndpoints.getPoliceDepartmentById(999);

        // Then
        assertThat(result).isCompleted();
        ResponseEntity<PoliceDepartmentApplicationDTO.Response> response = result.join();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNull();

        then(policeDepartmentRepository).should().GetByIdAsync(999);
    }

    @Test
    void getPoliceDepartmentByOverpassId_ShouldReturnPoliceDepartment_WhenPoliceDepartmentExists() {
        // Given
        given(policeDepartmentRepository.GetByOverpassIdAsync("way/123456789")).willReturn(CompletableFuture.completedFuture(Optional.of(testPoliceDepartment)));

        // When
        CompletableFuture<ResponseEntity<PoliceDepartmentApplicationDTO.Response>> result = policeDepartmentEndpoints.getPoliceDepartmentByOverpassId("way/123456789");

        // Then
        assertThat(result).isCompleted();
        ResponseEntity<PoliceDepartmentApplicationDTO.Response> response = result.join();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().overpassId()).isEqualTo("way/123456789");
        assertThat(response.getBody().name()).isEqualTo("1ª Delegacia de Polícia");

        then(policeDepartmentRepository).should().GetByOverpassIdAsync("way/123456789");
    }

    @Test
    void createPoliceDepartment_ShouldCreateAndReturnPoliceDepartment_WhenValidRequest() {
        // Given
        PoliceDepartmentPresentationDTO.CreateRequest request = new PoliceDepartmentPresentationDTO.CreateRequest(
                "way/555555555", "3ª Delegacia de Polícia", "3ª DP", "Polícia Civil", "Governo do Estado",
                "+55 11 7777-8888", "dp003@policia.sp.gov.br", "-23.5555", "-46.6555", 1);
        given(createPoliceDepartmentCommandHandler.handle(any(CreatePoliceDepartmentCommand.class)))
                .willReturn(CompletableFuture.completedFuture(testPoliceDepartment));

        // When
        CompletableFuture<ResponseEntity<PoliceDepartmentApplicationDTO.Response>> result = policeDepartmentEndpoints.createPoliceDepartment(request);

        // Then
        assertThat(result).isCompleted();
        ResponseEntity<PoliceDepartmentApplicationDTO.Response> response = result.join();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().name()).isEqualTo("1ª Delegacia de Polícia");

        then(createPoliceDepartmentCommandHandler).should().handle(argThat(command -> 
            command.overpassId().equals("way/555555555") &&
            command.name().equals("3ª Delegacia de Polícia") &&
            command.shortName().equals("3ª DP") &&
            command.operator().equals("Polícia Civil") &&
            command.addressId() == 1
        ));
    }

    @Test
    void createPoliceDepartment_ShouldReturnBadRequest_WhenCommandHandlerThrowsException() {
        // Given
        PoliceDepartmentPresentationDTO.CreateRequest request = new PoliceDepartmentPresentationDTO.CreateRequest(
                "", "Invalid", "", "", "", "", "", "0", "0", 999);
        given(createPoliceDepartmentCommandHandler.handle(any(CreatePoliceDepartmentCommand.class)))
                .willReturn(CompletableFuture.failedFuture(new IllegalArgumentException("Invalid police department data")));

        // When
        CompletableFuture<ResponseEntity<PoliceDepartmentApplicationDTO.Response>> result = policeDepartmentEndpoints.createPoliceDepartment(request);

        // Then
        assertThat(result).isCompleted();
        ResponseEntity<PoliceDepartmentApplicationDTO.Response> response = result.join();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNull();

        then(createPoliceDepartmentCommandHandler).should().handle(any(CreatePoliceDepartmentCommand.class));
    }

    @Test
    void updatePoliceDepartment_ShouldUpdateAndReturnPoliceDepartment_WhenPoliceDepartmentExists() {
        // Given
        PoliceDepartmentPresentationDTO.UpdateRequest request = new PoliceDepartmentPresentationDTO.UpdateRequest(
                "way/updated", "Updated Department", "Updated DP", "Updated Operator", "Updated Owner",
                "+55 11 9999-0000", "updated@policia.sp.gov.br", "-23.9999", "-46.9999", 1);
        PoliceDepartmentEntity updatedPoliceDepartment = spy(new PoliceDepartmentEntity());
        updatedPoliceDepartment.setId(1);
        updatedPoliceDepartment.setOverpassId("way/updated");
        updatedPoliceDepartment.setName("Updated Department");
        updatedPoliceDepartment.setAddress(testAddress);
        
        given(policeDepartmentRepository.GetByIdAsync(1)).willReturn(CompletableFuture.completedFuture(Optional.of(testPoliceDepartment)));
        given(policeDepartmentRepository.UpdateAsync(testPoliceDepartment)).willReturn(CompletableFuture.completedFuture(updatedPoliceDepartment));

        // When
        CompletableFuture<ResponseEntity<PoliceDepartmentApplicationDTO.Response>> result = policeDepartmentEndpoints.updatePoliceDepartment(1, request);

        // Then
        assertThat(result).isCompleted();
        ResponseEntity<PoliceDepartmentApplicationDTO.Response> response = result.join();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().overpassId()).isEqualTo("way/updated");
        assertThat(response.getBody().name()).isEqualTo("Updated Department");

        then(policeDepartmentRepository).should().GetByIdAsync(1);
        then(testPoliceDepartment).should().setOverpassId("way/updated");
        then(testPoliceDepartment).should().setName("Updated Department");
        then(policeDepartmentRepository).should().UpdateAsync(testPoliceDepartment);
    }

    @Test
    void updatePoliceDepartment_ShouldReturnNotFound_WhenPoliceDepartmentDoesNotExist() {
        // Given
        PoliceDepartmentPresentationDTO.UpdateRequest request = new PoliceDepartmentPresentationDTO.UpdateRequest(
                "way/nonexistent", "Non-existent", "NE", "None", "None", "", "", "0", "0", 1);
        given(policeDepartmentRepository.GetByIdAsync(999)).willReturn(CompletableFuture.completedFuture(Optional.empty()));

        // When
        CompletableFuture<ResponseEntity<PoliceDepartmentApplicationDTO.Response>> result = policeDepartmentEndpoints.updatePoliceDepartment(999, request);

        // Then
        assertThat(result).isCompleted();
        ResponseEntity<PoliceDepartmentApplicationDTO.Response> response = result.join();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNull();

        then(policeDepartmentRepository).should().GetByIdAsync(999);
        then(policeDepartmentRepository).should(never()).UpdateAsync(any());
    }

    @Test
    void deletePoliceDepartment_ShouldReturnNoContent_WhenPoliceDepartmentDeleted() {
        // Given
        given(policeDepartmentRepository.DeleteAsync(1)).willReturn(CompletableFuture.completedFuture(testPoliceDepartment));

        // When
        CompletableFuture<ResponseEntity<Void>> result = policeDepartmentEndpoints.deletePoliceDepartment(1);

        // Then
        assertThat(result).isCompleted();
        ResponseEntity<Void> response = result.join();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();

        then(policeDepartmentRepository).should().DeleteAsync(1);
    }

    @Test
    void deletePoliceDepartment_ShouldReturnNotFound_WhenDeleteFails() {
        // Given
        given(policeDepartmentRepository.DeleteAsync(999)).willReturn(CompletableFuture.failedFuture(new RuntimeException("Police department not found")));

        // When
        CompletableFuture<ResponseEntity<Void>> result = policeDepartmentEndpoints.deletePoliceDepartment(999);

        // Then
        assertThat(result).isCompleted();
        ResponseEntity<Void> response = result.join();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNull();

        then(policeDepartmentRepository).should().DeleteAsync(999);
    }
}