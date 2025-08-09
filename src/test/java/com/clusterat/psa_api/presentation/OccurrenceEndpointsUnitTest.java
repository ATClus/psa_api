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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
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
class OccurrenceEndpointsUnitTest {

    @Mock
    private IOccurrenceRepository occurrenceRepository;

    @Mock
    private CreateOccurrenceCommandHandler createOccurrenceCommandHandler;

    @InjectMocks
    private OccurrenceEndpoints occurrenceEndpoints;

    private OccurrenceEntity testOccurrence;
    private UserEntity testUser;
    private AddressEntity testAddress;
    private List<Optional<OccurrenceEntity>> testOccurrences;
    private LocalDateTime testDateTime;

    @BeforeEach
    void setUp() {
        testDateTime = LocalDateTime.of(2023, 12, 1, 10, 30, 0);
        
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
        testAddress.setComplement("Próximo à estação de metrô");
        testAddress.setNeighborhood("Centro");
        testAddress.setCity(testCity);

        testUser = spy(new UserEntity());
        testUser.setId(1);
        testUser.setCognitoId(12345);

        testOccurrence = spy(new OccurrenceEntity());
        testOccurrence.setId(1);
        testOccurrence.setName("Acidente de Trânsito");
        testOccurrence.setDescription("Acidente envolvendo dois veículos");
        testOccurrence.setDateStart(java.util.Date.from(testDateTime.atZone(java.time.ZoneId.systemDefault()).toInstant()));
        testOccurrence.setDateEnd(java.util.Date.from(testDateTime.plusHours(2).atZone(java.time.ZoneId.systemDefault()).toInstant()));
        testOccurrence.setDateUpdate(java.util.Date.from(testDateTime.plusHours(30).atZone(java.time.ZoneId.systemDefault()).toInstant()));
        testOccurrence.setActive(true);
        testOccurrence.setIntensity(Intensity.MODERATE);
        testOccurrence.setAddress(testAddress);
        testOccurrence.setUser(testUser);

        OccurrenceEntity testOccurrence2 = spy(new OccurrenceEntity());
        testOccurrence2.setId(2);
        testOccurrence2.setName("Roubo");
        testOccurrence2.setDescription("Roubo de veículo");
        testOccurrence2.setDateStart(java.util.Date.from(testDateTime.minusHours(1).atZone(java.time.ZoneId.systemDefault()).toInstant()));
        testOccurrence2.setDateEnd(java.util.Date.from(testDateTime.plusHours(1).atZone(java.time.ZoneId.systemDefault()).toInstant()));
        testOccurrence2.setDateUpdate(java.util.Date.from(testDateTime.atZone(java.time.ZoneId.systemDefault()).toInstant()));
        testOccurrence2.setActive(false);
        testOccurrence2.setIntensity(Intensity.HIGH);
        testOccurrence2.setAddress(testAddress);
        testOccurrence2.setUser(testUser);

        testOccurrences = Arrays.asList(Optional.of(testOccurrence), Optional.of(testOccurrence2));
    }

    @Test
    void getOccurrences_ShouldReturnListOfOccurrences_WhenOccurrencesExist() {
        // Given
        given(occurrenceRepository.GetAllAsync()).willReturn(CompletableFuture.completedFuture(testOccurrences));

        // When
        CompletableFuture<ResponseEntity<List<OccurrenceApplicationDTO.Response>>> result = occurrenceEndpoints.getOccurrences();

        // Then
        assertThat(result).isCompleted();
        ResponseEntity<List<OccurrenceApplicationDTO.Response>> response = result.join();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2);
        assertThat(response.getBody().get(0).id()).isEqualTo(1);
        assertThat(response.getBody().get(0).name()).isEqualTo("Acidente de Trânsito");
        assertThat(response.getBody().get(0).description()).isEqualTo("Acidente envolvendo dois veículos");
        assertThat(response.getBody().get(0).active()).isTrue();
        assertThat(response.getBody().get(0).intensity()).isEqualTo(Intensity.MODERATE);
        assertThat(response.getBody().get(0).addressId()).isEqualTo(1);
        assertThat(response.getBody().get(0).userId()).isEqualTo(1);

        then(occurrenceRepository).should().GetAllAsync();
    }

    @Test
    void getOccurrences_ShouldReturnInternalServerError_WhenRepositoryThrowsException() {
        // Given
        given(occurrenceRepository.GetAllAsync()).willReturn(CompletableFuture.failedFuture(new RuntimeException("Database error")));

        // When
        CompletableFuture<ResponseEntity<List<OccurrenceApplicationDTO.Response>>> result = occurrenceEndpoints.getOccurrences();

        // Then
        assertThat(result).isCompleted();
        ResponseEntity<List<OccurrenceApplicationDTO.Response>> response = result.join();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNull();

        then(occurrenceRepository).should().GetAllAsync();
    }

    @Test
    void getOccurrenceById_ShouldReturnOccurrence_WhenOccurrenceExists() {
        // Given
        given(occurrenceRepository.GetByIdAsync(1)).willReturn(CompletableFuture.completedFuture(Optional.of(testOccurrence)));

        // When
        CompletableFuture<ResponseEntity<OccurrenceApplicationDTO.Response>> result = occurrenceEndpoints.getOccurrenceById(1);

        // Then
        assertThat(result).isCompleted();
        ResponseEntity<OccurrenceApplicationDTO.Response> response = result.join();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isEqualTo(1);
        assertThat(response.getBody().name()).isEqualTo("Acidente de Trânsito");
        assertThat(response.getBody().description()).isEqualTo("Acidente envolvendo dois veículos");
        assertThat(response.getBody().active()).isTrue();
        assertThat(response.getBody().intensity()).isEqualTo(Intensity.MODERATE);
        assertThat(response.getBody().addressId()).isEqualTo(1);
        assertThat(response.getBody().userId()).isEqualTo(1);

        then(occurrenceRepository).should().GetByIdAsync(1);
    }

    @Test
    void getOccurrenceById_ShouldReturnNotFound_WhenOccurrenceDoesNotExist() {
        // Given
        given(occurrenceRepository.GetByIdAsync(999)).willReturn(CompletableFuture.completedFuture(Optional.empty()));

        // When
        CompletableFuture<ResponseEntity<OccurrenceApplicationDTO.Response>> result = occurrenceEndpoints.getOccurrenceById(999);

        // Then
        assertThat(result).isCompleted();
        ResponseEntity<OccurrenceApplicationDTO.Response> response = result.join();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNull();

        then(occurrenceRepository).should().GetByIdAsync(999);
    }

    @Test
    void getActiveOccurrences_ShouldReturnActiveOccurrences_WhenActiveOccurrencesExist() {
        // Given
        List<Optional<OccurrenceEntity>> activeOccurrences = List.of(Optional.of(testOccurrence));
        given(occurrenceRepository.GetByActiveAsync(true)).willReturn(CompletableFuture.completedFuture(activeOccurrences));

        // When
        CompletableFuture<ResponseEntity<List<OccurrenceApplicationDTO.Response>>> result = occurrenceEndpoints.getActiveOccurrences();

        // Then
        assertThat(result).isCompleted();
        ResponseEntity<List<OccurrenceApplicationDTO.Response>> response = result.join();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).active()).isTrue();

        then(occurrenceRepository).should().GetByActiveAsync(true);
    }

    @Test
    void getInactiveOccurrences_ShouldReturnInactiveOccurrences_WhenInactiveOccurrencesExist() {
        // Given
        OccurrenceEntity inactiveOccurrence = spy(new OccurrenceEntity());
        inactiveOccurrence.setId(3);
        inactiveOccurrence.setName("Ocorrência Finalizada");
        inactiveOccurrence.setActive(false);
        inactiveOccurrence.setIntensity(Intensity.LOW);
        inactiveOccurrence.setAddress(testAddress);
        inactiveOccurrence.setUser(testUser);
        
        List<Optional<OccurrenceEntity>> inactiveOccurrences = List.of(Optional.of(inactiveOccurrence));
        given(occurrenceRepository.GetByActiveAsync(false)).willReturn(CompletableFuture.completedFuture(inactiveOccurrences));

        // When
        CompletableFuture<ResponseEntity<List<OccurrenceApplicationDTO.Response>>> result = occurrenceEndpoints.getInactiveOccurrences();

        // Then
        assertThat(result).isCompleted();
        ResponseEntity<List<OccurrenceApplicationDTO.Response>> response = result.join();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).active()).isFalse();

        then(occurrenceRepository).should().GetByActiveAsync(false);
    }

    @Test
    void getOccurrencesByUserId_ShouldReturnUserOccurrences_WhenUserOccurrencesExist() {
        // Given
        given(occurrenceRepository.GetByUserIdAsync(1)).willReturn(CompletableFuture.completedFuture(testOccurrences));

        // When
        CompletableFuture<ResponseEntity<List<OccurrenceApplicationDTO.Response>>> result = occurrenceEndpoints.getOccurrencesByUserId(1);

        // Then
        assertThat(result).isCompleted();
        ResponseEntity<List<OccurrenceApplicationDTO.Response>> response = result.join();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2);
        assertThat(response.getBody().get(0).userId()).isEqualTo(1);
        assertThat(response.getBody().get(1).userId()).isEqualTo(1);

        then(occurrenceRepository).should().GetByUserIdAsync(1);
    }

    @Test
    void createOccurrence_ShouldCreateAndReturnOccurrence_WhenValidRequest() {
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
        CompletableFuture<ResponseEntity<OccurrenceApplicationDTO.Response>> result = occurrenceEndpoints.createOccurrence(request);

        // Then
        assertThat(result).isCompleted();
        ResponseEntity<OccurrenceApplicationDTO.Response> response = result.join();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().name()).isEqualTo("Acidente de Trânsito");

        then(createOccurrenceCommandHandler).should().handle(argThat(command -> 
            command.name().equals("Nova Ocorrência") &&
            command.description().equals("Descrição da nova ocorrência") &&
            command.active() &&
            command.intensity() == Intensity.HIGH &&
            command.addressId() == 1 &&
            command.userId() == 1
        ));
    }

    @Test
    void createOccurrence_ShouldReturnBadRequest_WhenCommandHandlerThrowsException() {
        // Given
        OccurrencePresentationDTO.CreateRequest request = new OccurrencePresentationDTO.CreateRequest(
                "", "",
                java.util.Date.from(testDateTime.atZone(java.time.ZoneId.systemDefault()).toInstant()),
                java.util.Date.from(testDateTime.atZone(java.time.ZoneId.systemDefault()).toInstant()),
                java.util.Date.from(testDateTime.atZone(java.time.ZoneId.systemDefault()).toInstant()),
                false, Intensity.LOW, 999, 999);
        given(createOccurrenceCommandHandler.handle(any(CreateOccurrenceCommand.class)))
                .willReturn(CompletableFuture.failedFuture(new IllegalArgumentException("Invalid occurrence data")));

        // When
        CompletableFuture<ResponseEntity<OccurrenceApplicationDTO.Response>> result = occurrenceEndpoints.createOccurrence(request);

        // Then
        assertThat(result).isCompleted();
        ResponseEntity<OccurrenceApplicationDTO.Response> response = result.join();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNull();

        then(createOccurrenceCommandHandler).should().handle(any(CreateOccurrenceCommand.class));
    }

    @Test
    void updateOccurrence_ShouldUpdateAndReturnOccurrence_WhenOccurrenceExists() {
        // Given
        OccurrencePresentationDTO.UpdateRequest request = new OccurrencePresentationDTO.UpdateRequest(
                "Ocorrência Atualizada", "Descrição atualizada",
                java.util.Date.from(testDateTime.atZone(java.time.ZoneId.systemDefault()).toInstant()),
                java.util.Date.from(testDateTime.plusHours(3).atZone(java.time.ZoneId.systemDefault()).toInstant()),
                java.util.Date.from(testDateTime.plusMinutes(45).atZone(java.time.ZoneId.systemDefault()).toInstant()),
                false, Intensity.SEVERE, 1, 1);
        OccurrenceEntity updatedOccurrence = spy(new OccurrenceEntity());
        updatedOccurrence.setId(1);
        updatedOccurrence.setName("Ocorrência Atualizada");
        updatedOccurrence.setDescription("Descrição atualizada");
        updatedOccurrence.setActive(false);
        updatedOccurrence.setIntensity(Intensity.SEVERE);
        updatedOccurrence.setAddress(testAddress);
        updatedOccurrence.setUser(testUser);
        
        given(occurrenceRepository.GetByIdAsync(1)).willReturn(CompletableFuture.completedFuture(Optional.of(testOccurrence)));
        given(occurrenceRepository.UpdateAsync(testOccurrence)).willReturn(CompletableFuture.completedFuture(updatedOccurrence));

        // When
        CompletableFuture<ResponseEntity<OccurrenceApplicationDTO.Response>> result = occurrenceEndpoints.updateOccurrence(1, request);

        // Then
        assertThat(result).isCompleted();
        ResponseEntity<OccurrenceApplicationDTO.Response> response = result.join();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().name()).isEqualTo("Ocorrência Atualizada");
        assertThat(response.getBody().description()).isEqualTo("Descrição atualizada");
        assertThat(response.getBody().active()).isFalse();
        assertThat(response.getBody().intensity()).isEqualTo(Intensity.SEVERE);

        then(occurrenceRepository).should().GetByIdAsync(1);
        then(testOccurrence).should().setName("Ocorrência Atualizada");
        then(testOccurrence).should().setDescription("Descrição atualizada");
        then(testOccurrence).should().setActive(false);
        then(testOccurrence).should().setIntensity(Intensity.SEVERE);
        then(occurrenceRepository).should().UpdateAsync(testOccurrence);
    }

    @Test
    void updateOccurrence_ShouldReturnNotFound_WhenOccurrenceDoesNotExist() {
        // Given
        OccurrencePresentationDTO.UpdateRequest request = new OccurrencePresentationDTO.UpdateRequest(
                "Non-existent", "Description",
                java.util.Date.from(testDateTime.atZone(java.time.ZoneId.systemDefault()).toInstant()),
                java.util.Date.from(testDateTime.atZone(java.time.ZoneId.systemDefault()).toInstant()),
                java.util.Date.from(testDateTime.atZone(java.time.ZoneId.systemDefault()).toInstant()),
                false, Intensity.LOW, 1, 1);
        given(occurrenceRepository.GetByIdAsync(999)).willReturn(CompletableFuture.completedFuture(Optional.empty()));

        // When
        CompletableFuture<ResponseEntity<OccurrenceApplicationDTO.Response>> result = occurrenceEndpoints.updateOccurrence(999, request);

        // Then
        assertThat(result).isCompleted();
        ResponseEntity<OccurrenceApplicationDTO.Response> response = result.join();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNull();

        then(occurrenceRepository).should().GetByIdAsync(999);
        then(occurrenceRepository).should(never()).UpdateAsync(any());
    }

    @Test
    void deleteOccurrence_ShouldReturnNoContent_WhenOccurrenceDeleted() {
        // Given
        given(occurrenceRepository.DeleteAsync(1)).willReturn(CompletableFuture.completedFuture(testOccurrence));

        // When
        CompletableFuture<ResponseEntity<Void>> result = occurrenceEndpoints.deleteOccurrence(1);

        // Then
        assertThat(result).isCompleted();
        ResponseEntity<Void> response = result.join();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();

        then(occurrenceRepository).should().DeleteAsync(1);
    }

    @Test
    void deleteOccurrence_ShouldReturnNotFound_WhenDeleteFails() {
        // Given
        given(occurrenceRepository.DeleteAsync(999)).willReturn(CompletableFuture.failedFuture(new RuntimeException("Occurrence not found")));

        // When
        CompletableFuture<ResponseEntity<Void>> result = occurrenceEndpoints.deleteOccurrence(999);

        // Then
        assertThat(result).isCompleted();
        ResponseEntity<Void> response = result.join();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNull();

        then(occurrenceRepository).should().DeleteAsync(999);
    }
}