package com.clusterat.psa_api.presentation;

import com.clusterat.psa_api.application.dto.AddressApplicationDTO;
import com.clusterat.psa_api.config.TestSecurityConfig;
import com.clusterat.psa_api.domain.value_objects.Region;
import com.clusterat.psa_api.presentation.dto.AddressPresentationDTO;
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
class AddressEndpointsIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ObjectMapper objectMapper;

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
    void shouldCreateCountryForAddressTests() throws Exception {
        // Given - Create a country first
        var countryCreateRequest = new com.clusterat.psa_api.presentation.dto.CountryPresentationDTO.CreateRequest(
                "Test Country for Address",
                "TCA",
                "TCA"
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
    void shouldCreateStateForAddressTests() throws Exception {
        // Given - Create a state for address dependency
        var stateCreateRequest = new com.clusterat.psa_api.presentation.dto.StatePresentationDTO.CreateRequest(
                "Test State for Address",
                "TSA",
                Region.SUDESTE,
                "33",
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
    void shouldCreateCityForAddressTests() throws Exception {
        // Given - Create a city for address dependency
        var cityCreateRequest = new com.clusterat.psa_api.presentation.dto.CityPresentationDTO.CreateRequest(
                "Test City for Address",
                "TCA",
                "3304557",
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
    void shouldCreateAddress() throws Exception {
        // Given
        AddressPresentationDTO.CreateRequest createRequest = new AddressPresentationDTO.CreateRequest(
                "Rua das Flores",
                "123",
                "Apt 45",
                "Centro",
                createdCityId
        );

        // When & Then
        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockUser("testadmin").roles("ADMIN"))
                .post()
                .uri("/api/v1/addresses")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(createRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(AddressApplicationDTO.Response.class)
                .value(response -> {
                    assertThat(response).isNotNull();
                    assertThat(response.id()).isNotNull();
                    assertThat(response.street()).isEqualTo("Rua das Flores");
                    assertThat(response.number()).isEqualTo("123");
                    assertThat(response.complement()).isEqualTo("Apt 45");
                    assertThat(response.neighborhood()).isEqualTo("Centro");
                    assertThat(response.cityId()).isEqualTo(createdCityId);
                    createdAddressId = response.id();
                });
    }

    @Test
    @Order(5)
    void shouldGetAllAddresses() {
        // When & Then
        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockUser("testuser").roles("USER"))
                .get()
                .uri("/api/v1/addresses")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(AddressApplicationDTO.Response.class)
                .value(addresses -> {
                    assertThat(addresses).isNotEmpty();
                    assertThat(addresses.get(0).street()).isNotNull();
                    assertThat(addresses.get(0).neighborhood()).isNotNull();
                });
    }

    @Test
    @Order(6)
    void shouldGetAddressById() {
        // When & Then
        if (createdAddressId != null) {
            webTestClient
                    .mutateWith(SecurityMockServerConfigurers.mockUser("testuser").roles("USER"))
                    .get()
                    .uri("/api/v1/addresses/{id}", createdAddressId)
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isOk()
                    .expectHeader().contentType(MediaType.APPLICATION_JSON)
                    .expectBody(AddressApplicationDTO.Response.class)
                    .value(address -> {
                        assertThat(address).isNotNull();
                        assertThat(address.id()).isEqualTo(createdAddressId);
                        assertThat(address.street()).isEqualTo("Rua das Flores");
                        assertThat(address.number()).isEqualTo("123");
                        assertThat(address.complement()).isEqualTo("Apt 45");
                        assertThat(address.neighborhood()).isEqualTo("Centro");
                    });
        }
    }

    @Test
    @Order(7)
    void shouldUpdateAddress() throws Exception {
        // Given
        AddressPresentationDTO.UpdateRequest updateRequest = new AddressPresentationDTO.UpdateRequest(
                "Avenida Principal",
                "456",
                "Bloco B",
                "Vila Nova",
                createdCityId
        );

        // When & Then
        if (createdAddressId != null) {
            webTestClient
                    .mutateWith(SecurityMockServerConfigurers.mockUser("testadmin").roles("ADMIN"))
                    .put()
                    .uri("/api/v1/addresses/{id}", createdAddressId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(updateRequest)
                    .exchange()
                    .expectStatus().isOk()
                    .expectHeader().contentType(MediaType.APPLICATION_JSON)
                    .expectBody(AddressApplicationDTO.Response.class)
                    .value(address -> {
                        assertThat(address).isNotNull();
                        assertThat(address.id()).isEqualTo(createdAddressId);
                        assertThat(address.street()).isEqualTo("Avenida Principal");
                        assertThat(address.number()).isEqualTo("456");
                        assertThat(address.complement()).isEqualTo("Bloco B");
                        assertThat(address.neighborhood()).isEqualTo("Vila Nova");
                    });
        }
    }

    @Test
    @Order(8)
    void shouldDeleteAddress() {
        // When & Then
        if (createdAddressId != null) {
            webTestClient
                    .mutateWith(SecurityMockServerConfigurers.mockUser("testadmin").roles("ADMIN"))
                    .delete()
                    .uri("/api/v1/addresses/{id}", createdAddressId)
                    .exchange()
                    .expectStatus().isNoContent()
                    .expectBody().isEmpty();
        }
    }

    @Test
    void shouldReturnNotFoundForNonExistentAddress() {
        // When & Then
        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockUser("testuser").roles("USER"))
                .get()
                .uri("/api/v1/addresses/{id}", 99999)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void shouldReturnBadRequestForInvalidAddressData() throws Exception {
        // Given - Invalid data (empty street)
        AddressPresentationDTO.CreateRequest invalidRequest = new AddressPresentationDTO.CreateRequest(
                "",  // Empty street
                "123",
                "Apt 45",
                "Centro",
                createdCityId != null ? createdCityId : 1
        );

        // When & Then
        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockUser("testadmin").roles("ADMIN"))
                .post()
                .uri("/api/v1/addresses")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidRequest)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void shouldReturnBadRequestForNullFields() throws Exception {
        // Given - Invalid data (null street)
        AddressPresentationDTO.CreateRequest invalidRequest = new AddressPresentationDTO.CreateRequest(
                null,  // Null street
                "123",
                "Apt 45",
                "Centro",
                createdCityId != null ? createdCityId : 1
        );

        // When & Then
        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockUser("testadmin").roles("ADMIN"))
                .post()
                .uri("/api/v1/addresses")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidRequest)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void shouldReturnBadRequestForInvalidCityId() throws Exception {
        // Given - Invalid city ID
        AddressPresentationDTO.CreateRequest invalidRequest = new AddressPresentationDTO.CreateRequest(
                "Rua das Flores",
                "123",
                "Apt 45",
                "Centro",
                -1  // Invalid city ID
        );

        // When & Then
        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockUser("testadmin").roles("ADMIN"))
                .post()
                .uri("/api/v1/addresses")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidRequest)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void shouldReturnBadRequestForEmptyNumber() throws Exception {
        // Given - Invalid data (empty number)
        AddressPresentationDTO.CreateRequest invalidRequest = new AddressPresentationDTO.CreateRequest(
                "Rua das Flores",
                "",  // Empty number
                "Apt 45",
                "Centro",
                createdCityId != null ? createdCityId : 1
        );

        // When & Then
        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockUser("testadmin").roles("ADMIN"))
                .post()
                .uri("/api/v1/addresses")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidRequest)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void shouldReturnBadRequestForEmptyNeighborhood() throws Exception {
        // Given - Invalid data (empty neighborhood)
        AddressPresentationDTO.CreateRequest invalidRequest = new AddressPresentationDTO.CreateRequest(
                "Rua das Flores",
                "123",
                "Apt 45",
                "",  // Empty neighborhood
                createdCityId != null ? createdCityId : 1
        );

        // When & Then
        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockUser("testadmin").roles("ADMIN"))
                .post()
                .uri("/api/v1/addresses")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidRequest)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void shouldReturnNotFoundForUpdateNonExistentAddress() throws Exception {
        // Given
        AddressPresentationDTO.UpdateRequest updateRequest = new AddressPresentationDTO.UpdateRequest(
                "Non Existent Street",
                "999",
                "Non Existent Complement",
                "Non Existent Neighborhood",
                createdCityId != null ? createdCityId : 1
        );

        // When & Then
        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockUser("testadmin").roles("ADMIN"))
                .put()
                .uri("/api/v1/addresses/{id}", 99999)
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
                .uri("/api/v1/addresses")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void shouldAllowBasicAuthentication() {
        // When & Then - Basic authentication
        webTestClient
                .get()
                .uri("/api/v1/addresses")
                .accept(MediaType.APPLICATION_JSON)
                .headers(headers -> headers.setBasicAuth("testuser", "testpassword"))
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void shouldRequireAdminRoleForCreate() {
        // Given
        AddressPresentationDTO.CreateRequest createRequest = new AddressPresentationDTO.CreateRequest(
                "Admin Test Street",
                "789",
                "Admin Complement",
                "Admin Neighborhood",
                createdCityId != null ? createdCityId : 1
        );

        // When & Then - USER role should not be able to create
        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockUser("testuser").roles("USER"))
                .post()
                .uri("/api/v1/addresses")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(createRequest)
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void shouldRequireAdminRoleForUpdate() {
        // Given
        AddressPresentationDTO.UpdateRequest updateRequest = new AddressPresentationDTO.UpdateRequest(
                "User Update Street",
                "101",
                "User Complement",
                "User Neighborhood",
                createdCityId != null ? createdCityId : 1
        );

        // When & Then - USER role should not be able to update
        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockUser("testuser").roles("USER"))
                .put()
                .uri("/api/v1/addresses/{id}", 1)
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
                .uri("/api/v1/addresses/{id}", 1)
                .exchange()
                .expectStatus().isForbidden();
    }
}