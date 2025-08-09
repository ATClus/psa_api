package com.clusterat.psa_api.presentation;

import com.clusterat.psa_api.application.commands.CreateOccurrenceCommand;
import com.clusterat.psa_api.application.dto.OccurrenceApplicationDTO;
import com.clusterat.psa_api.application.handlers.CreateOccurrenceCommandHandler;
import com.clusterat.psa_api.application.interfaces.IOccurrenceRepository;
import com.clusterat.psa_api.domain.entities.*;
import com.clusterat.psa_api.domain.value_objects.Intensity;
import com.clusterat.psa_api.domain.value_objects.Region;
import com.clusterat.psa_api.presentation.dto.OccurrencePresentationDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

/**
 * Advanced Mockito test class for OccurrenceEndpoints demonstrating:
 * - ArgumentCaptor usage
 * - Spy objects
 * - Verification with times() and never()
 * - Custom argument matchers
 * - Exception handling in async contexts
 * - InOrder verification
 * - BDD style testing
 */
@ExtendWith(MockitoExtension.class)
class OccurrenceEndpointsAdvancedMockitoTest {

    @Mock
    private IOccurrenceRepository occurrenceRepository;

    @Mock
    private CreateOccurrenceCommandHandler createOccurrenceCommandHandler;

    @InjectMocks
    private OccurrenceEndpoints occurrenceEndpoints;

    @Captor
    private ArgumentCaptor<CreateOccurrenceCommand> commandCaptor;

    private OccurrenceEntity testOccurrence;
    private UserEntity testUser;
    private AddressEntity testAddress;
    private LocalDateTime testDateTime;

    @BeforeEach
    void setUp() {
        testDateTime = LocalDateTime.of(2023, 12, 1, 10, 30, 0);
        
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
        testAddress.setComplement("Próximo à estação de metrô");
        testAddress.setNeighborhood("Centro");
        testAddress.setCity(testCity);

        testUser = new UserEntity();
        testUser.setId(1);
        testUser.setCognitoId(12345);

        testOccurrence = new OccurrenceEntity();
        testOccurrence.setId(1);
        testOccurrence.setName("Acidente de Trânsito");
        testOccurrence.setDescription("Acidente envolvendo dois veículos");
        testOccurrence.setDateStart(java.util.Date.from(testDateTime.atZone(java.time.ZoneId.systemDefault()).toInstant()));
        testOccurrence.setDateEnd(java.util.Date.from(testDateTime.plusHours(2).atZone(java.time.ZoneId.systemDefault()).toInstant()));
        testOccurrence.setDateUpdate(java.util.Date.from(testDateTime.plusMinutes(30).atZone(java.time.ZoneId.systemDefault()).toInstant()));
        testOccurrence.setActive(true);
        testOccurrence.setIntensity(Intensity.MODERATE);
        testOccurrence.setAddress(testAddress);
        testOccurrence.setUser(testUser);
    }

    @Test
    void createOccurrence_ShouldCaptureCorrectCommand_WhenCalledWithValidRequest() {
        // Given
        OccurrencePresentationDTO.CreateRequest request = new OccurrencePresentationDTO.CreateRequest(
            "Nova Ocorrência", "Descrição da nova ocorrência",
            java.util.Date.from(testDateTime.atZone(java.time.ZoneId.systemDefault()).toInstant()),
            java.util.Date.from(testDateTime.plusHours(1).atZone(java.time.ZoneId.systemDefault()).toInstant()),
            java.util.Date.from(testDateTime.plusMinutes(15).atZone(java.time.ZoneId.systemDefault()).toInstant()),
            true, Intensity.HIGH, 1, 1);
        given(createOccurrenceCommandHandler.handle(any(CreateOccurrenceCommand.class)))
                .willReturn(CompletableFuture.completedFuture(testOccurrence));

        // When
        occurrenceEndpoints.createOccurrence(request);

        // Then
        then(createOccurrenceCommandHandler).should().handle(commandCaptor.capture());
        CreateOccurrenceCommand capturedCommand = commandCaptor.getValue();
        
        assertThat(capturedCommand).isNotNull();
        assertThat(capturedCommand.name()).isEqualTo("Nova Ocorrência");
        assertThat(capturedCommand.description()).isEqualTo("Descrição da nova ocorrência");
        assertThat(capturedCommand.dateStart()).isEqualTo(java.util.Date.from(testDateTime.atZone(java.time.ZoneId.systemDefault()).toInstant()));
        assertThat(capturedCommand.dateEnd()).isEqualTo(java.util.Date.from(testDateTime.plusHours(1).atZone(java.time.ZoneId.systemDefault()).toInstant()));
        assertThat(capturedCommand.dateUpdate()).isEqualTo(java.util.Date.from(testDateTime.plusMinutes(15).atZone(java.time.ZoneId.systemDefault()).toInstant()));
        assertThat(capturedCommand.active()).isTrue();
        assertThat(capturedCommand.intensity()).isEqualTo(Intensity.HIGH);
        assertThat(capturedCommand.addressId()).isEqualTo(1);
        assertThat(capturedCommand.userId()).isEqualTo(1);
    }

    @Test
    void createOccurrence_ShouldCallHandlerExactlyOnce_WhenCalledMultipleTimes() {
        // Given
        OccurrencePresentationDTO.CreateRequest request = new OccurrencePresentationDTO.CreateRequest(
                "Acidente de Trânsito", "Acidente envolvendo dois veículos",
                java.util.Date.from(testDateTime.atZone(java.time.ZoneId.systemDefault()).toInstant()),
                java.util.Date.from(testDateTime.plusHours(2).atZone(java.time.ZoneId.systemDefault()).toInstant()),
                java.util.Date.from(testDateTime.plusMinutes(30).atZone(java.time.ZoneId.systemDefault()).toInstant()),
                true, Intensity.MODERATE, 1, 1);
        given(createOccurrenceCommandHandler.handle(any(CreateOccurrenceCommand.class)))
                .willReturn(CompletableFuture.completedFuture(testOccurrence));

        // When
        occurrenceEndpoints.createOccurrence(request);
        occurrenceEndpoints.createOccurrence(request);

        // Then
        then(createOccurrenceCommandHandler).should(times(2)).handle(any(CreateOccurrenceCommand.class));
    }

    @Test
    void getOccurrences_ShouldNeverCallRepository_WhenNotInvoked() {
        // When - Don't call getOccurrences()
        
        // Then
        then(occurrenceRepository).should(never()).GetAllAsync();
    }

    @Test
    void getOccurrenceById_ShouldUseCustomArgumentMatcher_WhenCalledWithPositiveId() {
        // Given
        given(occurrenceRepository.GetByIdAsync(argThat(id -> id > 0)))
                .willReturn(CompletableFuture.completedFuture(Optional.of(testOccurrence)));

        // When
        CompletableFuture<ResponseEntity<OccurrenceApplicationDTO.Response>> result = occurrenceEndpoints.getOccurrenceById(5);

        // Then
        assertThat(result.join().getStatusCode()).isEqualTo(HttpStatus.OK);
        then(occurrenceRepository).should().GetByIdAsync(argThat(id -> id > 0));
    }

    @Test
    void getOccurrences_ShouldHandleEmptyList_UsingSpyList() {
        // Given - Using a spy to track interactions with the list
        List<Optional<OccurrenceEntity>> emptyList = spy(new ArrayList<>());
        given(occurrenceRepository.GetAllAsync()).willReturn(CompletableFuture.completedFuture(emptyList));

        // When
        CompletableFuture<ResponseEntity<List<OccurrenceApplicationDTO.Response>>> result = occurrenceEndpoints.getOccurrences();

        // Then
        ResponseEntity<List<OccurrenceApplicationDTO.Response>> response = result.join();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();
        
        // Verify the spy list was accessed
        verify(emptyList).stream();
    }

    @Test
    void updateOccurrence_ShouldCallRepositoryInCorrectOrder_WhenUpdatingOccurrence() {
        // Given
        OccurrencePresentationDTO.UpdateRequest request = new OccurrencePresentationDTO.UpdateRequest(
                "Ocorrência Atualizada", "Descrição atualizada",
                java.util.Date.from(testDateTime.atZone(java.time.ZoneId.systemDefault()).toInstant()),
                java.util.Date.from(testDateTime.plusHours(3).atZone(java.time.ZoneId.systemDefault()).toInstant()),
                java.util.Date.from(testDateTime.plusMinutes(45).atZone(java.time.ZoneId.systemDefault()).toInstant()),
                false, Intensity.SEVERE, 1, 1);
        OccurrenceEntity updatedOccurrence = new OccurrenceEntity();
        updatedOccurrence.setId(1);
        updatedOccurrence.setName("Ocorrência Atualizada");
        updatedOccurrence.setAddress(testAddress);
        updatedOccurrence.setUser(testUser);

        given(occurrenceRepository.GetByIdAsync(1)).willReturn(CompletableFuture.completedFuture(Optional.of(testOccurrence)));
        given(occurrenceRepository.UpdateAsync(testOccurrence)).willReturn(CompletableFuture.completedFuture(updatedOccurrence));

        // When
        occurrenceEndpoints.updateOccurrence(1, request);

        // Then - Verify method call order
        InOrder inOrder = inOrder(occurrenceRepository);
        inOrder.verify(occurrenceRepository).GetByIdAsync(1);
        inOrder.verify(occurrenceRepository).UpdateAsync(testOccurrence);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    void deleteOccurrence_ShouldVerifyExactArguments_WhenCalledWithSpecificId() {
        // Given
        given(occurrenceRepository.DeleteAsync(eq(42))).willReturn(CompletableFuture.completedFuture(testOccurrence));

        // When
        occurrenceEndpoints.deleteOccurrence(42);

        // Then
        then(occurrenceRepository).should().DeleteAsync(eq(42));
        then(occurrenceRepository).should(never()).DeleteAsync(argThat(id -> !id.equals(42)));
    }

    @Test
    void createOccurrence_ShouldVerifyNoInteractionWithRepository_WhenHandlerIsUsed() {
        // Given
        OccurrencePresentationDTO.CreateRequest request = new OccurrencePresentationDTO.CreateRequest(
                "Nova Ocorrência", "Descrição da nova ocorrência",
                java.util.Date.from(testDateTime.atZone(java.time.ZoneId.systemDefault()).toInstant()),
                java.util.Date.from(testDateTime.plusHours(1).atZone(java.time.ZoneId.systemDefault()).toInstant()),
                java.util.Date.from(testDateTime.plusMinutes(15).atZone(java.time.ZoneId.systemDefault()).toInstant()),
                true, Intensity.HIGH, 1, 1);
        given(createOccurrenceCommandHandler.handle(any(CreateOccurrenceCommand.class)))
                .willReturn(CompletableFuture.completedFuture(testOccurrence));

        // When
        occurrenceEndpoints.createOccurrence(request);

        // Then - Verify no interaction with repository (only with command handler)
        then(occurrenceRepository).shouldHaveNoInteractions();
        then(createOccurrenceCommandHandler).should().handle(any(CreateOccurrenceCommand.class));
    }

    @Test
    void getActiveOccurrences_ShouldVerifyActiveParameterCorrectly() {
        // Given
        List<Optional<OccurrenceEntity>> activeOccurrences = List.of(Optional.of(testOccurrence));
        given(occurrenceRepository.GetByActiveAsync(true)).willReturn(CompletableFuture.completedFuture(activeOccurrences));

        // When
        occurrenceEndpoints.getActiveOccurrences();

        // Then
        then(occurrenceRepository).should().GetByActiveAsync(eq(true));
        then(occurrenceRepository).should(never()).GetByActiveAsync(eq(false));
    }

    @Test
    void getInactiveOccurrences_ShouldVerifyInactiveParameterCorrectly() {
        // Given
        List<Optional<OccurrenceEntity>> inactiveOccurrences = List.of(Optional.of(testOccurrence));
        given(occurrenceRepository.GetByActiveAsync(false)).willReturn(CompletableFuture.completedFuture(inactiveOccurrences));

        // When
        occurrenceEndpoints.getInactiveOccurrences();

        // Then
        then(occurrenceRepository).should().GetByActiveAsync(eq(false));
        then(occurrenceRepository).should(never()).GetByActiveAsync(eq(true));
    }

    @Test
    void getOccurrencesByUserId_ShouldVerifyUserIdParameter() {
        // Given
        List<Optional<OccurrenceEntity>> userOccurrences = List.of(Optional.of(testOccurrence));
        given(occurrenceRepository.GetByUserIdAsync(argThat(userId -> userId == 1)))
                .willReturn(CompletableFuture.completedFuture(userOccurrences));

        // When
        occurrenceEndpoints.getOccurrencesByUserId(1);

        // Then
        then(occurrenceRepository).should().GetByUserIdAsync(argThat(userId -> userId == 1));
    }

    @Test
    void createOccurrence_ShouldVerifyIntensityEnumValues_UsingMultipleCaptures() {
        // Given
        OccurrencePresentationDTO.CreateRequest request1 = new OccurrencePresentationDTO.CreateRequest(
                "Ocorrência Leve", "Descrição leve",
                java.util.Date.from(testDateTime.atZone(java.time.ZoneId.systemDefault()).toInstant()),
                java.util.Date.from(testDateTime.plusHours(1).atZone(java.time.ZoneId.systemDefault()).toInstant()),
                java.util.Date.from(testDateTime.plusMinutes(15).atZone(java.time.ZoneId.systemDefault()).toInstant()),
                true, Intensity.LOW, 1, 1);
        OccurrencePresentationDTO.CreateRequest request2 = new OccurrencePresentationDTO.CreateRequest(
                "Ocorrência Crítica", "Descrição crítica",
                java.util.Date.from(testDateTime.atZone(java.time.ZoneId.systemDefault()).toInstant()),
                java.util.Date.from(testDateTime.plusHours(1).atZone(java.time.ZoneId.systemDefault()).toInstant()),
                java.util.Date.from(testDateTime.plusMinutes(15).atZone(java.time.ZoneId.systemDefault()).toInstant()),
                true, Intensity.CRITICAL, 1, 1);
        given(createOccurrenceCommandHandler.handle(any(CreateOccurrenceCommand.class)))
                .willReturn(CompletableFuture.completedFuture(testOccurrence));

        // When
        occurrenceEndpoints.createOccurrence(request1);
        occurrenceEndpoints.createOccurrence(request2);

        // Then
        then(createOccurrenceCommandHandler).should(times(2)).handle(commandCaptor.capture());
        List<CreateOccurrenceCommand> capturedCommands = commandCaptor.getAllValues();
        
        assertThat(capturedCommands).hasSize(2);
        assertThat(capturedCommands.get(0).intensity()).isEqualTo(Intensity.LOW);
        assertThat(capturedCommands.get(0).name()).isEqualTo("Ocorrência Leve");
        assertThat(capturedCommands.get(1).intensity()).isEqualTo(Intensity.CRITICAL);
        assertThat(capturedCommands.get(1).name()).isEqualTo("Ocorrência Crítica");
    }

    @Test
    void updateOccurrence_ShouldCaptureEntityModifications_WhenUpdatingFields() {
        // Given
        OccurrenceEntity spyOccurrence = spy(new OccurrenceEntity());
        spyOccurrence.setId(1);
        spyOccurrence.setName("Original Name");
        spyOccurrence.setDescription("Original Description");
        spyOccurrence.setActive(true);
        spyOccurrence.setIntensity(Intensity.LOW);
        spyOccurrence.setAddress(testAddress);
        spyOccurrence.setUser(testUser);

        OccurrencePresentationDTO.UpdateRequest request = new OccurrencePresentationDTO.UpdateRequest(
                "New Name", "New Description",
                java.util.Date.from(testDateTime.atZone(java.time.ZoneId.systemDefault()).toInstant()),
                java.util.Date.from(testDateTime.plusHours(2).atZone(java.time.ZoneId.systemDefault()).toInstant()),
                java.util.Date.from(testDateTime.plusMinutes(30).atZone(java.time.ZoneId.systemDefault()).toInstant()),
                false, Intensity.HIGH, 1, 1);
        given(occurrenceRepository.GetByIdAsync(1)).willReturn(CompletableFuture.completedFuture(Optional.of(spyOccurrence)));
        given(occurrenceRepository.UpdateAsync(spyOccurrence)).willReturn(CompletableFuture.completedFuture(spyOccurrence));

        // When
        occurrenceEndpoints.updateOccurrence(1, request);

        // Then - Verify all setter methods were called with correct values
        then(spyOccurrence).should().setName("New Name");
        then(spyOccurrence).should().setDescription("New Description");
        then(spyOccurrence).should().setDateStart(java.util.Date.from(testDateTime.atZone(java.time.ZoneId.systemDefault()).toInstant()));
        then(spyOccurrence).should().setDateEnd(java.util.Date.from(testDateTime.plusHours(2).atZone(java.time.ZoneId.systemDefault()).toInstant()));
        then(spyOccurrence).should().setDateUpdate(java.util.Date.from(testDateTime.plusMinutes(30).atZone(java.time.ZoneId.systemDefault()).toInstant()));
        then(spyOccurrence).should().setActive(false);
        then(spyOccurrence).should().setIntensity(Intensity.HIGH);
        then(occurrenceRepository).should().UpdateAsync(spyOccurrence);
    }

    @Test
    void createOccurrence_ShouldVerifyDateTimeHandling_WhenCreatingOccurrenceWithSpecificDates() {
        // Given
        LocalDateTime startDate = LocalDateTime.of(2023, 1, 1, 8, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(2023, 1, 1, 18, 0, 0);
        LocalDateTime updateDate = LocalDateTime.of(2023, 1, 1, 12, 30, 0);
        
        OccurrencePresentationDTO.CreateRequest request = new OccurrencePresentationDTO.CreateRequest(
                "Ocorrência com Datas Específicas", "Teste de datas",
                java.util.Date.from(startDate.atZone(java.time.ZoneId.systemDefault()).toInstant()),
                java.util.Date.from(endDate.atZone(java.time.ZoneId.systemDefault()).toInstant()),
                java.util.Date.from(updateDate.atZone(java.time.ZoneId.systemDefault()).toInstant()),
                true, Intensity.MODERATE, 1, 1);
        given(createOccurrenceCommandHandler.handle(any(CreateOccurrenceCommand.class)))
                .willReturn(CompletableFuture.completedFuture(testOccurrence));

        // When
        occurrenceEndpoints.createOccurrence(request);

        // Then
        then(createOccurrenceCommandHandler).should().handle(commandCaptor.capture());
        CreateOccurrenceCommand capturedCommand = commandCaptor.getValue();
        
        assertThat(capturedCommand.dateStart()).isEqualTo(java.util.Date.from(startDate.atZone(java.time.ZoneId.systemDefault()).toInstant()));
        assertThat(capturedCommand.dateEnd()).isEqualTo(java.util.Date.from(endDate.atZone(java.time.ZoneId.systemDefault()).toInstant()));
        assertThat(capturedCommand.dateUpdate()).isEqualTo(java.util.Date.from(updateDate.atZone(java.time.ZoneId.systemDefault()).toInstant()));
    }
}
