package com.clusterat.psa_api.presentation;

import com.clusterat.psa_api.application.dto.OccurrenceApplicationDTO;
import com.clusterat.psa_api.config.TestSecurityConfig;
import com.clusterat.psa_api.domain.value_objects.Intensity;
import com.clusterat.psa_api.domain.value_objects.Region;
import com.clusterat.psa_api.presentation.dto.OccurrencePresentationDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
@Import(TestSecurityConfig.class)
@TestMethodOrder(OrderAnnotation.class)
@Transactional
class OccurrenceEndpointsIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ObjectMapper objectMapper;

    private static Integer createdOccurrenceId;
    private static Integer createdUserId;
    private static Integer createdAddressId;
    private static Integer createdCityId;
    private static Integer createdStateId;
    private static Integer createdCountryId;

    @BeforeEach
    void setUp() {
        webTestClient = webTestClient.mutate()
                .responseTimeout(Duration.ofSeconds(10))
                .build();
    }

    @Test
    @Order(1)
    void shouldCreateCountryForOccurrenceTests() throws Exception {
        // Given - Create a country first
        var countryCreateRequest = new com.clusterat.psa_api.presentation.dto.CountryPresentationDTO.CreateRequest(
                "Test Country for Occurrence",
                "TCO",
                "TCO"
        );

        // When & Then
        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockUser("testadmin").roles("ADMIN"))
                .post()
                .uri("/api/v1/countries")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(countryCreateRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(com.clusterat.psa_api.application.dto.CountryApplicationDTO.Response.class)
                .value(response -> {
                    assertThat(response).isNotNull();
                    assertThat(response.id()).isNotNull();
                    createdCountryId = response.id();
                });
    }

    @Test
    @Order(2)
    void shouldCreateStateForOccurrenceTests() throws Exception {
        // Given - Create a state
        var stateCreateRequest = new com.clusterat.psa_api.presentation.dto.StatePresentationDTO.CreateRequest(
                "Test State for Occurrence",
                "TSO",
                Region.SUDESTE,
                "41",
                createdCountryId
        );

        // When & Then
        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockUser("testadmin").roles("ADMIN"))
                .post()
                .uri("/api/v1/states")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(stateCreateRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(com.clusterat.psa_api.application.dto.StateApplicationDTO.Response.class)
                .value(response -> {
                    assertThat(response).isNotNull();
                    assertThat(response.id()).isNotNull();
                    createdStateId = response.id();
                });
    }

    @Test
    @Order(3)
    void shouldCreateCityForOccurrenceTests() throws Exception {
        // Given - Create a city
        var cityCreateRequest = new com.clusterat.psa_api.presentation.dto.CityPresentationDTO.CreateRequest(
                "Test City for Occurrence",
                "TCO",
                "4106902",
                createdStateId
        );

        // When & Then
        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockUser("testadmin").roles("ADMIN"))
                .post()
                .uri("/api/v1/cities")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(cityCreateRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(com.clusterat.psa_api.application.dto.CityApplicationDTO.Response.class)
                .value(response -> {
                    assertThat(response).isNotNull();
                    assertThat(response.id()).isNotNull();
                    createdCityId = response.id();
                });
    }

    @Test
    @Order(4)
    void shouldCreateAddressForOccurrenceTests() throws Exception {
        // Given - Create an address
        var addressCreateRequest = new com.clusterat.psa_api.presentation.dto.AddressPresentationDTO.CreateRequest(
                "Rua da Ocorrência",
                "200",
                "Casa 1",
                "Bairro Central",
                createdCityId
        );

        // When & Then
        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockUser("testadmin").roles("ADMIN"))
                .post()
                .uri("/api/v1/addresses")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(addressCreateRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(com.clusterat.psa_api.application.dto.AddressApplicationDTO.Response.class)
                .value(response -> {
                    assertThat(response).isNotNull();
                    assertThat(response.id()).isNotNull();
                    createdAddressId = response.id();
                });
    }

    @Test
    @Order(5)
    void shouldCreateUserForOccurrenceTests() throws Exception {
        // Given - Create a user for occurrence dependency
        var userCreateRequest = new com.clusterat.psa_api.presentation.dto.UserPresentationDTO.CreateRequest(987654321);

        // When & Then
        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockUser("testadmin").roles("ADMIN"))
                .post()
                .uri("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(userCreateRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(com.clusterat.psa_api.application.dto.UserApplicationDTO.Response.class)
                .value(response -> {
                    assertThat(response).isNotNull();
                    assertThat(response.id()).isNotNull();
                    createdUserId = response.id();
                });
    }

    @Test
    @Order(6)
    void shouldCreateOccurrence() throws Exception {
        // Given
        Date now = new Date();
        Date endDate = new Date(now.getTime() + 86400000); // Tomorrow
        Date updateDate = new Date(now.getTime() + 3600000); // 1 hour later

        OccurrencePresentationDTO.CreateRequest createRequest = new OccurrencePresentationDTO.CreateRequest(
                "Acidente de Trânsito",
                "Colisão entre dois veículos na Rua da Ocorrência",
                now,
                endDate,
                updateDate,
                true,
                Intensity.HIGH,
                createdAddressId,
                createdUserId
        );

        // When & Then
        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockUser("testadmin").roles("ADMIN"))
                .post()
                .uri("/api/v1/occurrences")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(createRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(OccurrenceApplicationDTO.Response.class)
                .value(response -> {
                    assertThat(response).isNotNull();
                    assertThat(response.id()).isNotNull();
                    assertThat(response.name()).isEqualTo("Acidente de Trânsito");
                    assertThat(response.description()).isEqualTo("Colisão entre dois veículos na Rua da Ocorrência");
                    assertThat(response.active()).isTrue();
                    assertThat(response.intensity()).isEqualTo(Intensity.HIGH);
                    assertThat(response.addressId()).isEqualTo(createdAddressId);
                    assertThat(response.userId()).isEqualTo(createdUserId);
                    createdOccurrenceId = response.id();
                });
    }

    @Test
    @Order(7)
    void shouldGetAllOccurrences() {
        // When & Then
        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockUser("testuser").roles("USER"))
                .get()
                .uri("/api/v1/occurrences")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(OccurrenceApplicationDTO.Response.class)
                .value(occurrences -> {
                    assertThat(occurrences).isNotEmpty();
                    assertThat(occurrences.get(0).name()).isNotNull();
                    assertThat(occurrences.get(0).intensity()).isNotNull();
                });
    }

    @Test
    @Order(8)
    void shouldGetOccurrenceById() {
        // When & Then
        if (createdOccurrenceId != null) {
            webTestClient
                    .mutateWith(SecurityMockServerConfigurers.mockUser("testuser").roles("USER"))
                    .get()
                    .uri("/api/v1/occurrences/{id}", createdOccurrenceId)
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isOk()
                    .expectHeader().contentType(MediaType.APPLICATION_JSON)
                    .expectBody(OccurrenceApplicationDTO.Response.class)
                    .value(occurrence -> {
                        assertThat(occurrence).isNotNull();
                        assertThat(occurrence.id()).isEqualTo(createdOccurrenceId);
                        assertThat(occurrence.name()).isEqualTo("Acidente de Trânsito");
                        assertThat(occurrence.description()).isEqualTo("Colisão entre dois veículos na Rua da Ocorrência");
                        assertThat(occurrence.intensity()).isEqualTo(Intensity.HIGH);
                    });
        }
    }

    @Test
    @Order(9)
    void shouldGetActiveOccurrences() {
        // When & Then
        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockUser("testuser").roles("USER"))
                .get()
                .uri("/api/v1/occurrences/active")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(OccurrenceApplicationDTO.Response.class)
                .value(occurrences -> {
                    assertThat(occurrences).isNotEmpty();
                    occurrences.forEach(occurrence -> assertThat(occurrence.active()).isTrue());
                });
    }

    @Test
    @Order(10)
    void shouldGetInactiveOccurrences() {
        // When & Then
        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockUser("testuser").roles("USER"))
                .get()
                .uri("/api/v1/occurrences/inactive")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(OccurrenceApplicationDTO.Response.class)
                .value(occurrences -> {
                    occurrences.forEach(occurrence -> assertThat(occurrence.active()).isFalse());
                });
    }

    @Test
    @Order(11)
    void shouldGetOccurrencesByUserId() {
        // When & Then
        if (createdUserId != null) {
            webTestClient
                    .mutateWith(SecurityMockServerConfigurers.mockUser("testuser").roles("USER"))
                    .get()
                    .uri("/api/v1/occurrences/user/{userId}", createdUserId)
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isOk()
                    .expectHeader().contentType(MediaType.APPLICATION_JSON)
                    .expectBodyList(OccurrenceApplicationDTO.Response.class)
                    .value(occurrences -> {
                        assertThat(occurrences).isNotEmpty();
                        occurrences.forEach(occurrence -> assertThat(occurrence.userId()).isEqualTo(createdUserId));
                    });
        }
    }

    @Test
    @Order(12)
    void shouldUpdateOccurrence() throws Exception {
        // Given
        Date now = new Date();
        Date endDate = new Date(now.getTime() + 172800000); // Day after tomorrow
        Date updateDate = new Date(now.getTime() + 7200000); // 2 hours later

        OccurrencePresentationDTO.UpdateRequest updateRequest = new OccurrencePresentationDTO.UpdateRequest(
                "Acidente de Trânsito Atualizado",
                "Colisão resolvida, trânsito liberado",
                now,
                endDate,
                updateDate,
                false, // Now inactive
                Intensity.LOW, // Reduced intensity
                createdAddressId,
                createdUserId
        );

        // When & Then
        if (createdOccurrenceId != null) {
            webTestClient
                    .mutateWith(SecurityMockServerConfigurers.mockUser("testadmin").roles("ADMIN"))
                    .put()
                    .uri("/api/v1/occurrences/{id}", createdOccurrenceId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(updateRequest)
                    .exchange()
                    .expectStatus().isOk()
                    .expectHeader().contentType(MediaType.APPLICATION_JSON)
                    .expectBody(OccurrenceApplicationDTO.Response.class)
                    .value(occurrence -> {
                        assertThat(occurrence).isNotNull();
                        assertThat(occurrence.id()).isEqualTo(createdOccurrenceId);
                        assertThat(occurrence.name()).isEqualTo("Acidente de Trânsito Atualizado");
                        assertThat(occurrence.description()).isEqualTo("Colisão resolvida, trânsito liberado");
                        assertThat(occurrence.active()).isFalse();
                        assertThat(occurrence.intensity()).isEqualTo(Intensity.LOW);
                    });
        }
    }

    @Test
    @Order(13)
    void shouldDeleteOccurrence() {
        // When & Then
        if (createdOccurrenceId != null) {
            webTestClient
                    .mutateWith(SecurityMockServerConfigurers.mockUser("testadmin").roles("ADMIN"))
                    .delete()
                    .uri("/api/v1/occurrences/{id}", createdOccurrenceId)
                    .exchange()
                    .expectStatus().isNoContent()
                    .expectBody().isEmpty();
        }
    }

    @Test
    void shouldReturnNotFoundForNonExistentOccurrence() {
        // When & Then
        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockUser("testuser").roles("USER"))
                .get()
                .uri("/api/v1/occurrences/{id}", 99999)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void shouldReturnBadRequestForInvalidOccurrenceData() throws Exception {
        // Given - Invalid data (empty name)
        Date now = new Date();
        OccurrencePresentationDTO.CreateRequest invalidRequest = new OccurrencePresentationDTO.CreateRequest(
                "",  // Empty name
                "Valid description",
                now,
                null,
                null,
                true,
                Intensity.MODERATE,
                createdAddressId != null ? createdAddressId : 1,
                createdUserId != null ? createdUserId : 1
        );

        // When & Then
        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockUser("testadmin").roles("ADMIN"))
                .post()
                .uri("/api/v1/occurrences")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidRequest)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void shouldReturnBadRequestForNullFields() throws Exception {
        // Given - Invalid data (null description)
        Date now = new Date();
        OccurrencePresentationDTO.CreateRequest invalidRequest = new OccurrencePresentationDTO.CreateRequest(
                "Valid Name",
                null,  // Null description
                now,
                null,
                null,
                true,
                Intensity.MODERATE,
                createdAddressId != null ? createdAddressId : 1,
                createdUserId != null ? createdUserId : 1
        );

        // When & Then
        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockUser("testadmin").roles("ADMIN"))
                .post()
                .uri("/api/v1/occurrences")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidRequest)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void shouldReturnBadRequestForInvalidAddressId() throws Exception {
        // Given - Invalid address ID
        Date now = new Date();
        OccurrencePresentationDTO.CreateRequest invalidRequest = new OccurrencePresentationDTO.CreateRequest(
                "Valid Name",
                "Valid description",
                now,
                null,
                null,
                true,
                Intensity.MODERATE,
                -1,  // Invalid address ID
                createdUserId != null ? createdUserId : 1
        );

        // When & Then
        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockUser("testadmin").roles("ADMIN"))
                .post()
                .uri("/api/v1/occurrences")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidRequest)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void shouldReturnBadRequestForInvalidUserId() throws Exception {
        // Given - Invalid user ID
        Date now = new Date();
        OccurrencePresentationDTO.CreateRequest invalidRequest = new OccurrencePresentationDTO.CreateRequest(
                "Valid Name",
                "Valid description",
                now,
                null,
                null,
                true,
                Intensity.MODERATE,
                createdAddressId != null ? createdAddressId : 1,
                -1  // Invalid user ID
        );

        // When & Then
        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockUser("testadmin").roles("ADMIN"))
                .post()
                .uri("/api/v1/occurrences")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidRequest)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void shouldReturnNotFoundForUpdateNonExistentOccurrence() throws Exception {
        // Given
        Date now = new Date();
        OccurrencePresentationDTO.UpdateRequest updateRequest = new OccurrencePresentationDTO.UpdateRequest(
                "Non Existent Occurrence",
                "This occurrence does not exist",
                now,
                null,
                null,
                false,
                Intensity.LOW,
                createdAddressId != null ? createdAddressId : 1,
                createdUserId != null ? createdUserId : 1
        );

        // When & Then
        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockUser("testadmin").roles("ADMIN"))
                .put()
                .uri("/api/v1/occurrences/{id}", 99999)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updateRequest)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void shouldRequireAuthentication() {
        // When & Then - No authentication
        webTestClient
                .get()
                .uri("/api/v1/occurrences")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void shouldAllowBasicAuthentication() {
        // When & Then - Basic authentication
        webTestClient
                .get()
                .uri("/api/v1/occurrences")
                .accept(MediaType.APPLICATION_JSON)
                .headers(headers -> headers.setBasicAuth("testuser", "testpassword"))
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void shouldRequireAdminRoleForCreate() {
        // Given
        Date now = new Date();
        OccurrencePresentationDTO.CreateRequest createRequest = new OccurrencePresentationDTO.CreateRequest(
                "Admin Test Occurrence",
                "This requires admin privileges",
                now,
                null,
                null,
                true,
                Intensity.CRITICAL,
                createdAddressId != null ? createdAddressId : 1,
                createdUserId != null ? createdUserId : 1
        );

        // When & Then - USER role should not be able to create
        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockUser("testuser").roles("USER"))
                .post()
                .uri("/api/v1/occurrences")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(createRequest)
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void shouldRequireAdminRoleForUpdate() {
        // Given
        Date now = new Date();
        OccurrencePresentationDTO.UpdateRequest updateRequest = new OccurrencePresentationDTO.UpdateRequest(
                "User Update Occurrence",
                "User tries to update",
                now,
                null,
                null,
                false,
                Intensity.LOW,
                createdAddressId != null ? createdAddressId : 1,
                createdUserId != null ? createdUserId : 1
        );

        // When & Then - USER role should not be able to update
        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockUser("testuser").roles("USER"))
                .put()
                .uri("/api/v1/occurrences/{id}", 1)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updateRequest)
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void shouldRequireAdminRoleForDelete() {
        // When & Then - USER role should not be able to delete
        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockUser("testuser").roles("USER"))
                .delete()
                .uri("/api/v1/occurrences/{id}", 1)
                .exchange()
                .expectStatus().isForbidden();
    }
}