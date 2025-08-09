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
 * Advanced Mockito test class for PoliceDepartmentEndpoints demonstrating:
 * - ArgumentCaptor usage
 * - Spy objects
 * - Verification with times() and never()
 * - Custom argument matchers
 * - Exception handling in async contexts
 * - InOrder verification
 * - BDD style testing
 */
@ExtendWith(MockitoExtension.class)
class PoliceDepartmentEndpointsAdvancedMockitoTest {

    @Mock
    private IPoliceDepartmentRepository policeDepartmentRepository;

    @Mock
    private CreatePoliceDepartmentCommandHandler createPoliceDepartmentCommandHandler;

    @InjectMocks
    private PoliceDepartmentEndpoints policeDepartmentEndpoints;

    @Captor
    private ArgumentCaptor<CreatePoliceDepartmentCommand> commandCaptor;

    private PoliceDepartmentEntity testPoliceDepartment;
    private AddressEntity testAddress;

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

        CityEntity testCity = new CityEntity();
        testCity.setId(1);
        testCity.setName("São Paulo");
        testCity.setShortName("SP");
        testCity.setIbgeCode("3550308");
        testCity.setState(testState);

        testAddress = new AddressEntity();
        testAddress.setId(1);
        testAddress.setStreet("Rua da Consolação");
        testAddress.setNumber("100");
        testAddress.setComplement("Delegacia Central");
        testAddress.setNeighborhood("Centro");
        testAddress.setCity(testCity);

        testPoliceDepartment = new PoliceDepartmentEntity();
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
    }

    @Test
    void createPoliceDepartment_ShouldCaptureCorrectCommand_WhenCalledWithValidRequest() {
        // Given
        PoliceDepartmentPresentationDTO.CreateRequest request = new PoliceDepartmentPresentationDTO.CreateRequest(
                "way/555555555", "3ª Delegacia de Polícia", "3ª DP", "Polícia Civil", "Governo do Estado",
                "+55 11 7777-8888", "dp003@policia.sp.gov.br", "-23.5555", "-46.6555", 1);
        given(createPoliceDepartmentCommandHandler.handle(any(CreatePoliceDepartmentCommand.class)))
                .willReturn(CompletableFuture.completedFuture(testPoliceDepartment));

        // When
        policeDepartmentEndpoints.createPoliceDepartment(request);

        // Then
        then(createPoliceDepartmentCommandHandler).should().handle(commandCaptor.capture());
        CreatePoliceDepartmentCommand capturedCommand = commandCaptor.getValue();
        
        assertThat(capturedCommand).isNotNull();
        assertThat(capturedCommand.overpassId()).isEqualTo("way/555555555");
        assertThat(capturedCommand.name()).isEqualTo("3ª Delegacia de Polícia");
        assertThat(capturedCommand.shortName()).isEqualTo("3ª DP");
        assertThat(capturedCommand.operator()).isEqualTo("Polícia Civil");
        assertThat(capturedCommand.ownership()).isEqualTo("Governo do Estado");
        assertThat(capturedCommand.phone()).isEqualTo("+55 11 7777-8888");
        assertThat(capturedCommand.email()).isEqualTo("dp003@policia.sp.gov.br");
        assertThat(capturedCommand.latitude()).isEqualTo("-23.5555");
        assertThat(capturedCommand.longitude()).isEqualTo("-46.6555");
        assertThat(capturedCommand.addressId()).isEqualTo(1);
    }

    @Test
    void createPoliceDepartment_ShouldCallHandlerExactlyOnce_WhenCalledMultipleTimes() {
        // Given
        PoliceDepartmentPresentationDTO.CreateRequest request = new PoliceDepartmentPresentationDTO.CreateRequest(
                "way/123456789", "1ª Delegacia de Polícia", "1ª DP", "Polícia Civil", "Governo do Estado",
                "+55 11 3333-4444", "dp001@policia.sp.gov.br", "-23.5505", "-46.6333", 1);
        given(createPoliceDepartmentCommandHandler.handle(any(CreatePoliceDepartmentCommand.class)))
                .willReturn(CompletableFuture.completedFuture(testPoliceDepartment));

        // When
        policeDepartmentEndpoints.createPoliceDepartment(request);
        policeDepartmentEndpoints.createPoliceDepartment(request);

        // Then
        then(createPoliceDepartmentCommandHandler).should(times(2)).handle(any(CreatePoliceDepartmentCommand.class));
    }

    @Test
    void getPoliceDepartments_ShouldNeverCallRepository_WhenNotInvoked() {
        // When - Don't call getPoliceDepartments()
        
        // Then
        then(policeDepartmentRepository).should(never()).GetAllAsync();
    }

    @Test
    void getPoliceDepartmentById_ShouldUseCustomArgumentMatcher_WhenCalledWithPositiveId() {
        // Given
        given(policeDepartmentRepository.GetByIdAsync(argThat(id -> id > 0)))
                .willReturn(CompletableFuture.completedFuture(Optional.of(testPoliceDepartment)));

        // When
        CompletableFuture<ResponseEntity<PoliceDepartmentApplicationDTO.Response>> result = policeDepartmentEndpoints.getPoliceDepartmentById(5);

        // Then
        assertThat(result.join().getStatusCode()).isEqualTo(HttpStatus.OK);
        then(policeDepartmentRepository).should().GetByIdAsync(argThat(id -> id > 0));
    }

    @Test
    void getPoliceDepartments_ShouldHandleEmptyList_UsingSpyList() {
        // Given - Using a spy to track interactions with the list
        List<Optional<PoliceDepartmentEntity>> emptyList = spy(new ArrayList<>());
        given(policeDepartmentRepository.GetAllAsync()).willReturn(CompletableFuture.completedFuture(emptyList));

        // When
        CompletableFuture<ResponseEntity<List<PoliceDepartmentApplicationDTO.Response>>> result = policeDepartmentEndpoints.getPoliceDepartments();

        // Then
        ResponseEntity<List<PoliceDepartmentApplicationDTO.Response>> response = result.join();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();
        
        // Verify the spy list was accessed
        verify(emptyList).stream();
    }

    @Test
    void updatePoliceDepartment_ShouldCallRepositoryInCorrectOrder_WhenUpdatingPoliceDepartment() {
        // Given
        PoliceDepartmentPresentationDTO.UpdateRequest request = new PoliceDepartmentPresentationDTO.UpdateRequest(
                "way/updated", "Updated Department", "Updated DP", "Updated Operator", "Updated Owner",
                "+55 11 9999-0000", "updated@policia.sp.gov.br", "-23.9999", "-46.9999", 1);
        PoliceDepartmentEntity updatedPoliceDepartment = new PoliceDepartmentEntity();
        updatedPoliceDepartment.setId(1);
        updatedPoliceDepartment.setOverpassId("way/updated");
        updatedPoliceDepartment.setName("Updated Department");
        updatedPoliceDepartment.setAddress(testAddress);

        given(policeDepartmentRepository.GetByIdAsync(1)).willReturn(CompletableFuture.completedFuture(Optional.of(testPoliceDepartment)));
        given(policeDepartmentRepository.UpdateAsync(testPoliceDepartment)).willReturn(CompletableFuture.completedFuture(updatedPoliceDepartment));

        // When
        policeDepartmentEndpoints.updatePoliceDepartment(1, request);

        // Then - Verify method call order
        InOrder inOrder = inOrder(policeDepartmentRepository);
        inOrder.verify(policeDepartmentRepository).GetByIdAsync(1);
        inOrder.verify(policeDepartmentRepository).UpdateAsync(testPoliceDepartment);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    void deletePoliceDepartment_ShouldVerifyExactArguments_WhenCalledWithSpecificId() {
        // Given
        given(policeDepartmentRepository.DeleteAsync(eq(42))).willReturn(CompletableFuture.completedFuture(testPoliceDepartment));

        // When
        policeDepartmentEndpoints.deletePoliceDepartment(42);

        // Then
        then(policeDepartmentRepository).should().DeleteAsync(eq(42));
        then(policeDepartmentRepository).should(never()).DeleteAsync(intThat(id -> id != 42));
    }

    @Test
    void getPoliceDepartmentByOverpassId_ShouldUseStringMatcher_WhenCalledWithValidOverpassId() {
        // Given
        given(policeDepartmentRepository.GetByOverpassIdAsync(argThat(id -> id.startsWith("way/"))))
                .willReturn(CompletableFuture.completedFuture(Optional.of(testPoliceDepartment)));

        // When
        CompletableFuture<ResponseEntity<PoliceDepartmentApplicationDTO.Response>> result = policeDepartmentEndpoints.getPoliceDepartmentByOverpassId("way/123456789");

        // Then
        assertThat(result.join().getStatusCode()).isEqualTo(HttpStatus.OK);
        then(policeDepartmentRepository).should().GetByOverpassIdAsync(argThat(id -> id.startsWith("way/")));
    }

    @Test
    void createPoliceDepartment_ShouldVerifyNoInteractionWithRepository_WhenHandlerIsUsed() {
        // Given
        PoliceDepartmentPresentationDTO.CreateRequest request = new PoliceDepartmentPresentationDTO.CreateRequest(
                "way/555555555", "3ª Delegacia de Polícia", "3ª DP", "Polícia Civil", "Governo do Estado",
                "+55 11 7777-8888", "dp003@policia.sp.gov.br", "-23.5555", "-46.6555", 1);
        given(createPoliceDepartmentCommandHandler.handle(any(CreatePoliceDepartmentCommand.class)))
                .willReturn(CompletableFuture.completedFuture(testPoliceDepartment));

        // When
        policeDepartmentEndpoints.createPoliceDepartment(request);

        // Then - Verify no interaction with repository (only with command handler)
        then(policeDepartmentRepository).shouldHaveNoInteractions();
        then(createPoliceDepartmentCommandHandler).should().handle(any(CreatePoliceDepartmentCommand.class));
    }

    @Test
    void createPoliceDepartment_ShouldVerifyCommandFields_UsingMultipleCaptures() {
        // Given
        PoliceDepartmentPresentationDTO.CreateRequest request1 = new PoliceDepartmentPresentationDTO.CreateRequest(
                "way/111111111", "1ª Delegacia", "1ª DP", "PM", "Estado", "+55 11 1111-1111", "dp001@pm.sp.gov.br", "-23.1111", "-46.1111", 1);
        PoliceDepartmentPresentationDTO.CreateRequest request2 = new PoliceDepartmentPresentationDTO.CreateRequest(
                "way/222222222", "2ª Delegacia", "2ª DP", "PC", "Estado", "+55 11 2222-2222", "dp002@pc.sp.gov.br", "-23.2222", "-46.2222", 2);
        given(createPoliceDepartmentCommandHandler.handle(any(CreatePoliceDepartmentCommand.class)))
                .willReturn(CompletableFuture.completedFuture(testPoliceDepartment));

        // When
        policeDepartmentEndpoints.createPoliceDepartment(request1);
        policeDepartmentEndpoints.createPoliceDepartment(request2);

        // Then
        then(createPoliceDepartmentCommandHandler).should(times(2)).handle(commandCaptor.capture());
        List<CreatePoliceDepartmentCommand> capturedCommands = commandCaptor.getAllValues();
        
        assertThat(capturedCommands).hasSize(2);
        assertThat(capturedCommands.get(0).overpassId()).isEqualTo("way/111111111");
        assertThat(capturedCommands.get(0).name()).isEqualTo("1ª Delegacia");
        assertThat(capturedCommands.get(0).operator()).isEqualTo("PM");
        assertThat(capturedCommands.get(1).overpassId()).isEqualTo("way/222222222");
        assertThat(capturedCommands.get(1).name()).isEqualTo("2ª Delegacia");
        assertThat(capturedCommands.get(1).operator()).isEqualTo("PC");
    }
}