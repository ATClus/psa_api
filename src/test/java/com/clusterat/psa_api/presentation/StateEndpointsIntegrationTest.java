package com.clusterat.psa_api.presentation;

import com.clusterat.psa_api.application.dto.StateApplicationDTO;
import com.clusterat.psa_api.config.TestSecurityConfig;
import com.clusterat.psa_api.domain.value_objects.Region;
import com.clusterat.psa_api.presentation.dto.StatePresentationDTO;
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

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
@Import(TestSecurityConfig.class)
@TestMethodOrder(OrderAnnotation.class)
@Transactional
class StateEndpointsIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ObjectMapper objectMapper;

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
    void shouldCreateCountryForStateTests() throws Exception {
        // Given - Create a country first for state dependency
        var countryCreateRequest = new com.clusterat.psa_api.presentation.dto.CountryPresentationDTO.CreateRequest(
                "Test Country for State",
                "TCS",
                "TST"
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
    void shouldCreateState() throws Exception {
        // Given
        StatePresentationDTO.CreateRequest createRequest = new StatePresentationDTO.CreateRequest(
                "Test State",
                "TS",
                Region.SUDESTE,
                "35",
                createdCountryId
        );

        // When & Then
        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockUser("testadmin").roles("ADMIN"))
                .post()
                .uri("/api/v1/states")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(createRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(StateApplicationDTO.Response.class)
                .value(response -> {
                    assertThat(response).isNotNull();
                    assertThat(response.id()).isNotNull();
                    assertThat(response.name()).isEqualTo("Test State");
                    assertThat(response.shortName()).isEqualTo("TS");
                    assertThat(response.region()).isEqualTo(Region.SUDESTE);
                    assertThat(response.ibgeCode()).isEqualTo("35");
                    assertThat(response.countryId()).isEqualTo(createdCountryId);
                    createdStateId = response.id();
                });
    }

    @Test
    @Order(3)
    void shouldGetAllStates() {
        // When & Then
        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockUser("testuser").roles("USER"))
                .get()
                .uri("/api/v1/states")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(StateApplicationDTO.Response.class)
                .value(states -> {
                    assertThat(states).isNotEmpty();
                    assertThat(states.get(0).name()).isNotNull();
                    assertThat(states.get(0).region()).isNotNull();
                });
    }

    @Test
    @Order(4)
    void shouldGetStateById() {
        // When & Then
        if (createdStateId != null) {
            webTestClient
                    .mutateWith(SecurityMockServerConfigurers.mockUser("testuser").roles("USER"))
                    .get()
                    .uri("/api/v1/states/{id}", createdStateId)
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isOk()
                    .expectHeader().contentType(MediaType.APPLICATION_JSON)
                    .expectBody(StateApplicationDTO.Response.class)
                    .value(state -> {
                        assertThat(state).isNotNull();
                        assertThat(state.id()).isEqualTo(createdStateId);
                        assertThat(state.name()).isEqualTo("Test State");
                        assertThat(state.shortName()).isEqualTo("TS");
                        assertThat(state.region()).isEqualTo(Region.SUDESTE);
                        assertThat(state.ibgeCode()).isEqualTo("35");
                    });
        }
    }

    @Test
    @Order(5)
    void shouldGetStateByIbgeCode() {
        // When & Then
        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockUser("testuser").roles("USER"))
                .get()
                .uri("/api/v1/states/ibge/{ibgeCode}", "35")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(StateApplicationDTO.Response.class)
                .value(state -> {
                    assertThat(state).isNotNull();
                    assertThat(state.ibgeCode()).isEqualTo("35");
                    assertThat(state.name()).isEqualTo("Test State");
                    assertThat(state.region()).isEqualTo(Region.SUDESTE);
                });
    }

    @Test
    @Order(6)
    void shouldDeleteState() {
        // When & Then
        if (createdStateId != null) {
            webTestClient
                    .mutateWith(SecurityMockServerConfigurers.mockUser("testadmin").roles("ADMIN"))
                    .delete()
                    .uri("/api/v1/states/{id}", createdStateId)
                    .exchange()
                    .expectStatus().isNoContent()
                    .expectBody().isEmpty();
        }
    }

    @Test
    void shouldReturnNotFoundForNonExistentState() {
        // When & Then
        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockUser("testuser").roles("USER"))
                .get()
                .uri("/api/v1/states/{id}", 99999)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void shouldReturnNotFoundForNonExistentIbgeCode() {
        // When & Then
        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockUser("testuser").roles("USER"))
                .get()
                .uri("/api/v1/states/ibge/{ibgeCode}", "999")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void shouldReturnBadRequestForInvalidStateData() throws Exception {
        // Given - Invalid data (empty name)
        StatePresentationDTO.CreateRequest invalidRequest = new StatePresentationDTO.CreateRequest(
                "",  // Empty name
                "TS",
                Region.SUDESTE,
                "35",
                createdCountryId != null ? createdCountryId : 1
        );

        // When & Then
        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockUser("testadmin").roles("ADMIN"))
                .post()
                .uri("/api/v1/states")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidRequest)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void shouldReturnBadRequestForNullFields() throws Exception {
        // Given - Invalid data (null name)
        StatePresentationDTO.CreateRequest invalidRequest = new StatePresentationDTO.CreateRequest(
                null,  // Null name
                "TS",
                Region.SUDESTE,
                "35",
                createdCountryId != null ? createdCountryId : 1
        );

        // When & Then
        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockUser("testadmin").roles("ADMIN"))
                .post()
                .uri("/api/v1/states")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidRequest)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void shouldReturnBadRequestForInvalidCountryId() throws Exception {
        // Given - Invalid country ID
        StatePresentationDTO.CreateRequest invalidRequest = new StatePresentationDTO.CreateRequest(
                "Test State",
                "TS",
                Region.SUDESTE,
                "35",
                -1  // Invalid country ID
        );

        // When & Then
        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockUser("testadmin").roles("ADMIN"))
                .post()
                .uri("/api/v1/states")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidRequest)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void shouldRequireAuthentication() {
        // When & Then - No authentication
        webTestClient
                .get()
                .uri("/api/v1/states")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void shouldAllowBasicAuthentication() {
        // When & Then - Basic authentication
        webTestClient
                .get()
                .uri("/api/v1/states")
                .accept(MediaType.APPLICATION_JSON)
                .headers(headers -> headers.setBasicAuth("testuser", "testpassword"))
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void shouldRequireAdminRoleForCreate() {
        // Given
        StatePresentationDTO.CreateRequest createRequest = new StatePresentationDTO.CreateRequest(
                "Test State Admin",
                "TSA",
                Region.SUL,
                "42",
                createdCountryId != null ? createdCountryId : 1
        );

        // When & Then - USER role should not be able to create
        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockUser("testuser").roles("USER"))
                .post()
                .uri("/api/v1/states")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(createRequest)
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void shouldRequireAdminRoleForDelete() {
        // When & Then - USER role should not be able to delete
        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockUser("testuser").roles("USER"))
                .delete()
                .uri("/api/v1/states/{id}", 1)
                .exchange()
                .expectStatus().isForbidden();
    }
}