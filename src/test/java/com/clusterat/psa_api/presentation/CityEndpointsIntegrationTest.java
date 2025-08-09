package com.clusterat.psa_api.presentation;

import com.clusterat.psa_api.application.dto.CityApplicationDTO;
import com.clusterat.psa_api.config.TestSecurityConfig;
import com.clusterat.psa_api.domain.value_objects.Region;
import com.clusterat.psa_api.presentation.dto.CityPresentationDTO;
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
class CityEndpointsIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ObjectMapper objectMapper;

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
    void shouldCreateCountryForCityTests() throws Exception {
        // Given - Create a country first
        var countryCreateRequest = new com.clusterat.psa_api.presentation.dto.CountryPresentationDTO.CreateRequest(
                "Test Country for City",
                "TCC",
                "TCC"
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
    void shouldCreateStateForCityTests() throws Exception {
        // Given - Create a state for city dependency
        var stateCreateRequest = new com.clusterat.psa_api.presentation.dto.StatePresentationDTO.CreateRequest(
                "Test State for City",
                "TSC",
                Region.SUDESTE,
                "31",
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
    void shouldCreateCity() throws Exception {
        // Given
        CityPresentationDTO.CreateRequest createRequest = new CityPresentationDTO.CreateRequest(
                "Test City",
                "TC",
                "3106200",
                createdStateId
        );

        // When & Then
        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockUser("testadmin").roles("ADMIN"))
                .post()
                .uri("/api/v1/cities")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(createRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(CityApplicationDTO.Response.class)
                .value(response -> {
                    assertThat(response).isNotNull();
                    assertThat(response.id()).isNotNull();
                    assertThat(response.name()).isEqualTo("Test City");
                    assertThat(response.shortName()).isEqualTo("TC");
                    assertThat(response.ibgeCode()).isEqualTo("3106200");
                    assertThat(response.stateId()).isEqualTo(createdStateId);
                    createdCityId = response.id();
                });
    }

    @Test
    @Order(4)
    void shouldGetAllCities() {
        // When & Then
        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockUser("testuser").roles("USER"))
                .get()
                .uri("/api/v1/cities")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(CityApplicationDTO.Response.class)
                .value(cities -> {
                    assertThat(cities).isNotEmpty();
                    assertThat(cities.get(0).name()).isNotNull();
                    assertThat(cities.get(0).ibgeCode()).isNotNull();
                });
    }

    @Test
    @Order(5)
    void shouldGetCityById() {
        // When & Then
        if (createdCityId != null) {
            webTestClient
                    .mutateWith(SecurityMockServerConfigurers.mockUser("testuser").roles("USER"))
                    .get()
                    .uri("/api/v1/cities/{id}", createdCityId)
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isOk()
                    .expectHeader().contentType(MediaType.APPLICATION_JSON)
                    .expectBody(CityApplicationDTO.Response.class)
                    .value(city -> {
                        assertThat(city).isNotNull();
                        assertThat(city.id()).isEqualTo(createdCityId);
                        assertThat(city.name()).isEqualTo("Test City");
                        assertThat(city.shortName()).isEqualTo("TC");
                        assertThat(city.ibgeCode()).isEqualTo("3106200");
                    });
        }
    }

    @Test
    @Order(6)
    void shouldGetCityByIbgeCode() {
        // When & Then
        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockUser("testuser").roles("USER"))
                .get()
                .uri("/api/v1/cities/ibge/{ibgeCode}", "3106200")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(CityApplicationDTO.Response.class)
                .value(city -> {
                    assertThat(city).isNotNull();
                    assertThat(city.ibgeCode()).isEqualTo("3106200");
                    assertThat(city.name()).isEqualTo("Test City");
                    assertThat(city.shortName()).isEqualTo("TC");
                });
    }

    @Test
    @Order(7)
    void shouldUpdateCity() throws Exception {
        // Given
        CityPresentationDTO.UpdateRequest updateRequest = new CityPresentationDTO.UpdateRequest(
                "Updated Test City",
                "UTC",
                "3106201",
                createdStateId
        );

        // When & Then
        if (createdCityId != null) {
            webTestClient
                    .mutateWith(SecurityMockServerConfigurers.mockUser("testadmin").roles("ADMIN"))
                    .put()
                    .uri("/api/v1/cities/{id}", createdCityId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(updateRequest)
                    .exchange()
                    .expectStatus().isOk()
                    .expectHeader().contentType(MediaType.APPLICATION_JSON)
                    .expectBody(CityApplicationDTO.Response.class)
                    .value(city -> {
                        assertThat(city).isNotNull();
                        assertThat(city.id()).isEqualTo(createdCityId);
                        assertThat(city.name()).isEqualTo("Updated Test City");
                        assertThat(city.shortName()).isEqualTo("UTC");
                        assertThat(city.ibgeCode()).isEqualTo("3106201");
                    });
        }
    }

    @Test
    @Order(8)
    void shouldDeleteCity() {
        // When & Then
        if (createdCityId != null) {
            webTestClient
                    .mutateWith(SecurityMockServerConfigurers.mockUser("testadmin").roles("ADMIN"))
                    .delete()
                    .uri("/api/v1/cities/{id}", createdCityId)
                    .exchange()
                    .expectStatus().isNoContent()
                    .expectBody().isEmpty();
        }
    }

    @Test
    void shouldReturnNotFoundForNonExistentCity() {
        // When & Then
        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockUser("testuser").roles("USER"))
                .get()
                .uri("/api/v1/cities/{id}", 99999)
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
                .uri("/api/v1/cities/ibge/{ibgeCode}", "9999999")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void shouldReturnBadRequestForInvalidCityData() throws Exception {
        // Given - Invalid data (empty name)
        CityPresentationDTO.CreateRequest invalidRequest = new CityPresentationDTO.CreateRequest(
                "",  // Empty name
                "TC",
                "3106200",
                createdStateId != null ? createdStateId : 1
        );

        // When & Then
        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockUser("testadmin").roles("ADMIN"))
                .post()
                .uri("/api/v1/cities")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidRequest)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void shouldReturnBadRequestForNullFields() throws Exception {
        // Given - Invalid data (null name)
        CityPresentationDTO.CreateRequest invalidRequest = new CityPresentationDTO.CreateRequest(
                null,  // Null name
                "TC",
                "3106200",
                createdStateId != null ? createdStateId : 1
        );

        // When & Then
        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockUser("testadmin").roles("ADMIN"))
                .post()
                .uri("/api/v1/cities")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidRequest)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void shouldReturnBadRequestForInvalidStateId() throws Exception {
        // Given - Invalid state ID
        CityPresentationDTO.CreateRequest invalidRequest = new CityPresentationDTO.CreateRequest(
                "Test City",
                "TC",
                "3106200",
                -1  // Invalid state ID
        );

        // When & Then
        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockUser("testadmin").roles("ADMIN"))
                .post()
                .uri("/api/v1/cities")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidRequest)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void shouldReturnNotFoundForUpdateNonExistentCity() throws Exception {
        // Given
        CityPresentationDTO.UpdateRequest updateRequest = new CityPresentationDTO.UpdateRequest(
                "Non Existent City",
                "NEC",
                "9999999",
                createdStateId != null ? createdStateId : 1
        );

        // When & Then
        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockUser("testadmin").roles("ADMIN"))
                .put()
                .uri("/api/v1/cities/{id}", 99999)
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
                .uri("/api/v1/cities")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void shouldAllowBasicAuthentication() {
        // When & Then - Basic authentication
        webTestClient
                .get()
                .uri("/api/v1/cities")
                .accept(MediaType.APPLICATION_JSON)
                .headers(headers -> headers.setBasicAuth("testuser", "testpassword"))
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void shouldRequireAdminRoleForCreate() {
        // Given
        CityPresentationDTO.CreateRequest createRequest = new CityPresentationDTO.CreateRequest(
                "Test City Admin",
                "TCA",
                "3106205",
                createdStateId != null ? createdStateId : 1
        );

        // When & Then - USER role should not be able to create
        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockUser("testuser").roles("USER"))
                .post()
                .uri("/api/v1/cities")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(createRequest)
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void shouldRequireAdminRoleForUpdate() {
        // Given
        CityPresentationDTO.UpdateRequest updateRequest = new CityPresentationDTO.UpdateRequest(
                "User Update Attempt",
                "UUA",
                "3106206",
                createdStateId != null ? createdStateId : 1
        );

        // When & Then - USER role should not be able to update
        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockUser("testuser").roles("USER"))
                .put()
                .uri("/api/v1/cities/{id}", 1)
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
                .uri("/api/v1/cities/{id}", 1)
                .exchange()
                .expectStatus().isForbidden();
    }
}