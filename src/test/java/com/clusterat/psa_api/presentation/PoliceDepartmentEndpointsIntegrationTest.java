package com.clusterat.psa_api.presentation;

import com.clusterat.psa_api.application.dto.PoliceDepartmentApplicationDTO;
import com.clusterat.psa_api.config.TestSecurityConfig;
import com.clusterat.psa_api.domain.value_objects.Region;
import com.clusterat.psa_api.presentation.dto.PoliceDepartmentPresentationDTO;
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
class PoliceDepartmentEndpointsIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ObjectMapper objectMapper;

    private static Integer createdPoliceDepartmentId;
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
    void shouldCreateCountryForPoliceDepartmentTests() throws Exception {
        // Given - Create a country first
        var countryCreateRequest = new com.clusterat.psa_api.presentation.dto.CountryPresentationDTO.CreateRequest(
                "Test Country for Police Dept",
                "TCP",
                "TCP"
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
    void shouldCreateStateForPoliceDepartmentTests() throws Exception {
        // Given - Create a state
        var stateCreateRequest = new com.clusterat.psa_api.presentation.dto.StatePresentationDTO.CreateRequest(
                "Test State for Police Dept",
                "TSP",
                Region.SUDESTE,
                "43",
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
    void shouldCreateCityForPoliceDepartmentTests() throws Exception {
        // Given - Create a city
        var cityCreateRequest = new com.clusterat.psa_api.presentation.dto.CityPresentationDTO.CreateRequest(
                "Test City for Police Dept",
                "TCP",
                "4314902",
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
    void shouldCreateAddressForPoliceDepartmentTests() throws Exception {
        // Given - Create an address for police department dependency
        var addressCreateRequest = new com.clusterat.psa_api.presentation.dto.AddressPresentationDTO.CreateRequest(
                "Rua da Delegacia",
                "100",
                "Térreo",
                "Centro",
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
    void shouldCreatePoliceDepartment() throws Exception {
        // Given
        PoliceDepartmentPresentationDTO.CreateRequest createRequest = new PoliceDepartmentPresentationDTO.CreateRequest(
                "osm-123456789",
                "Delegacia Central",
                "DC",
                "Polícia Civil",
                "Público",
                "+55 11 1234-5678",
                "delegacia.central@policia.gov.br",
                "-23.5505",
                "-46.6333",
                createdAddressId
        );

        // When & Then
        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockUser("testadmin").roles("ADMIN"))
                .post()
                .uri("/api/v1/police-departments")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(createRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(PoliceDepartmentApplicationDTO.Response.class)
                .value(response -> {
                    assertThat(response).isNotNull();
                    assertThat(response.id()).isNotNull();
                    assertThat(response.overpassId()).isEqualTo("osm-123456789");
                    assertThat(response.name()).isEqualTo("Delegacia Central");
                    assertThat(response.shortName()).isEqualTo("DC");
                    assertThat(response.operator()).isEqualTo("Polícia Civil");
                    assertThat(response.ownership()).isEqualTo("Público");
                    assertThat(response.phone()).isEqualTo("+55 11 1234-5678");
                    assertThat(response.email()).isEqualTo("delegacia.central@policia.gov.br");
                    assertThat(response.latitude()).isEqualTo("-23.5505");
                    assertThat(response.longitude()).isEqualTo("-46.6333");
                    assertThat(response.addressId()).isEqualTo(createdAddressId);
                    createdPoliceDepartmentId = response.id();
                });
    }

    @Test
    @Order(6)
    void shouldGetAllPoliceDepartments() {
        // When & Then
        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockUser("testuser").roles("USER"))
                .get()
                .uri("/api/v1/police-departments")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(PoliceDepartmentApplicationDTO.Response.class)
                .value(departments -> {
                    assertThat(departments).isNotEmpty();
                    assertThat(departments.get(0).name()).isNotNull();
                    assertThat(departments.get(0).operator()).isNotNull();
                });
    }

    @Test
    @Order(7)
    void shouldGetPoliceDepartmentById() {
        // When & Then
        if (createdPoliceDepartmentId != null) {
            webTestClient
                    .mutateWith(SecurityMockServerConfigurers.mockUser("testuser").roles("USER"))
                    .get()
                    .uri("/api/v1/police-departments/{id}", createdPoliceDepartmentId)
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isOk()
                    .expectHeader().contentType(MediaType.APPLICATION_JSON)
                    .expectBody(PoliceDepartmentApplicationDTO.Response.class)
                    .value(department -> {
                        assertThat(department).isNotNull();
                        assertThat(department.id()).isEqualTo(createdPoliceDepartmentId);
                        assertThat(department.name()).isEqualTo("Delegacia Central");
                        assertThat(department.overpassId()).isEqualTo("osm-123456789");
                        assertThat(department.operator()).isEqualTo("Polícia Civil");
                    });
        }
    }

    @Test
    @Order(8)
    void shouldGetPoliceDepartmentByOverpassId() {
        // When & Then
        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockUser("testuser").roles("USER"))
                .get()
                .uri("/api/v1/police-departments/overpass/{overpassId}", "osm-123456789")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(PoliceDepartmentApplicationDTO.Response.class)
                .value(department -> {
                    assertThat(department).isNotNull();
                    assertThat(department.overpassId()).isEqualTo("osm-123456789");
                    assertThat(department.name()).isEqualTo("Delegacia Central");
                    assertThat(department.operator()).isEqualTo("Polícia Civil");
                });
    }

    @Test
    @Order(9)
    void shouldUpdatePoliceDepartment() throws Exception {
        // Given
        PoliceDepartmentPresentationDTO.UpdateRequest updateRequest = new PoliceDepartmentPresentationDTO.UpdateRequest(
                "osm-987654321",
                "Delegacia Central Atualizada",
                "DCA",
                "Polícia Militar",
                "Público",
                "+55 11 9876-5432",
                "delegacia.atualizada@policia.gov.br",
                "-23.5555",
                "-46.6444",
                createdAddressId
        );

        // When & Then
        if (createdPoliceDepartmentId != null) {
            webTestClient
                    .mutateWith(SecurityMockServerConfigurers.mockUser("testadmin").roles("ADMIN"))
                    .put()
                    .uri("/api/v1/police-departments/{id}", createdPoliceDepartmentId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(updateRequest)
                    .exchange()
                    .expectStatus().isOk()
                    .expectHeader().contentType(MediaType.APPLICATION_JSON)
                    .expectBody(PoliceDepartmentApplicationDTO.Response.class)
                    .value(department -> {
                        assertThat(department).isNotNull();
                        assertThat(department.id()).isEqualTo(createdPoliceDepartmentId);
                        assertThat(department.overpassId()).isEqualTo("osm-987654321");
                        assertThat(department.name()).isEqualTo("Delegacia Central Atualizada");
                        assertThat(department.shortName()).isEqualTo("DCA");
                        assertThat(department.operator()).isEqualTo("Polícia Militar");
                        assertThat(department.phone()).isEqualTo("+55 11 9876-5432");
                        assertThat(department.email()).isEqualTo("delegacia.atualizada@policia.gov.br");
                    });
        }
    }

    @Test
    @Order(10)
    void shouldDeletePoliceDepartment() {
        // When & Then
        if (createdPoliceDepartmentId != null) {
            webTestClient
                    .mutateWith(SecurityMockServerConfigurers.mockUser("testadmin").roles("ADMIN"))
                    .delete()
                    .uri("/api/v1/police-departments/{id}", createdPoliceDepartmentId)
                    .exchange()
                    .expectStatus().isNoContent()
                    .expectBody().isEmpty();
        }
    }

    @Test
    void shouldReturnNotFoundForNonExistentPoliceDepartment() {
        // When & Then
        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockUser("testuser").roles("USER"))
                .get()
                .uri("/api/v1/police-departments/{id}", 99999)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void shouldReturnNotFoundForNonExistentOverpassId() {
        // When & Then
        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockUser("testuser").roles("USER"))
                .get()
                .uri("/api/v1/police-departments/overpass/{overpassId}", "non-existent-id")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void shouldReturnBadRequestForInvalidPoliceDepartmentData() throws Exception {
        // Given - Invalid data (empty name)
        PoliceDepartmentPresentationDTO.CreateRequest invalidRequest = new PoliceDepartmentPresentationDTO.CreateRequest(
                "osm-invalid",
                "",  // Empty name
                "DC",
                "Polícia Civil",
                "Público",
                "+55 11 1234-5678",
                "invalid@policia.gov.br",
                "-23.5505",
                "-46.6333",
                createdAddressId != null ? createdAddressId : 1
        );

        // When & Then
        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockUser("testadmin").roles("ADMIN"))
                .post()
                .uri("/api/v1/police-departments")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidRequest)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void shouldReturnBadRequestForNullFields() throws Exception {
        // Given - Invalid data (null operator)
        PoliceDepartmentPresentationDTO.CreateRequest invalidRequest = new PoliceDepartmentPresentationDTO.CreateRequest(
                "osm-invalid2",
                "Test Department",
                "TD",
                null,  // Null operator
                "Público",
                "+55 11 1234-5678",
                "invalid2@policia.gov.br",
                "-23.5505",
                "-46.6333",
                createdAddressId != null ? createdAddressId : 1
        );

        // When & Then
        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockUser("testadmin").roles("ADMIN"))
                .post()
                .uri("/api/v1/police-departments")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidRequest)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void shouldReturnBadRequestForInvalidEmail() throws Exception {
        // Given - Invalid email format
        PoliceDepartmentPresentationDTO.CreateRequest invalidRequest = new PoliceDepartmentPresentationDTO.CreateRequest(
                "osm-invalid-email",
                "Test Department",
                "TD",
                "Polícia Civil",
                "Público",
                "+55 11 1234-5678",
                "invalid-email",  // Invalid email
                "-23.5505",
                "-46.6333",
                createdAddressId != null ? createdAddressId : 1
        );

        // When & Then
        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockUser("testadmin").roles("ADMIN"))
                .post()
                .uri("/api/v1/police-departments")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidRequest)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void shouldReturnBadRequestForInvalidAddressId() throws Exception {
        // Given - Invalid address ID
        PoliceDepartmentPresentationDTO.CreateRequest invalidRequest = new PoliceDepartmentPresentationDTO.CreateRequest(
                "osm-invalid-address",
                "Test Department",
                "TD",
                "Polícia Civil",
                "Público",
                "+55 11 1234-5678",
                "test@policia.gov.br",
                "-23.5505",
                "-46.6333",
                -1  // Invalid address ID
        );

        // When & Then
        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockUser("testadmin").roles("ADMIN"))
                .post()
                .uri("/api/v1/police-departments")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidRequest)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void shouldReturnNotFoundForUpdateNonExistentPoliceDepartment() throws Exception {
        // Given
        PoliceDepartmentPresentationDTO.UpdateRequest updateRequest = new PoliceDepartmentPresentationDTO.UpdateRequest(
                "osm-non-existent",
                "Non Existent Department",
                "NED",
                "Polícia Civil",
                "Público",
                "+55 11 0000-0000",
                "nonexistent@policia.gov.br",
                "0.0000",
                "0.0000",
                createdAddressId != null ? createdAddressId : 1
        );

        // When & Then
        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockUser("testadmin").roles("ADMIN"))
                .put()
                .uri("/api/v1/police-departments/{id}", 99999)
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
                .uri("/api/v1/police-departments")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void shouldAllowBasicAuthentication() {
        // When & Then - Basic authentication
        webTestClient
                .get()
                .uri("/api/v1/police-departments")
                .accept(MediaType.APPLICATION_JSON)
                .headers(headers -> headers.setBasicAuth("testuser", "testpassword"))
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void shouldRequireAdminRoleForCreate() {
        // Given
        PoliceDepartmentPresentationDTO.CreateRequest createRequest = new PoliceDepartmentPresentationDTO.CreateRequest(
                "osm-admin-test",
                "Admin Test Department",
                "ATD",
                "Polícia Civil",
                "Público",
                "+55 11 1111-1111",
                "admin.test@policia.gov.br",
                "-23.0000",
                "-46.0000",
                createdAddressId != null ? createdAddressId : 1
        );

        // When & Then - USER role should not be able to create
        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockUser("testuser").roles("USER"))
                .post()
                .uri("/api/v1/police-departments")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(createRequest)
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void shouldRequireAdminRoleForUpdate() {
        // Given
        PoliceDepartmentPresentationDTO.UpdateRequest updateRequest = new PoliceDepartmentPresentationDTO.UpdateRequest(
                "osm-user-update",
                "User Update Department",
                "UUD",
                "Polícia Civil",
                "Público",
                "+55 11 2222-2222",
                "user.update@policia.gov.br",
                "-23.1111",
                "-46.1111",
                createdAddressId != null ? createdAddressId : 1
        );

        // When & Then - USER role should not be able to update
        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockUser("testuser").roles("USER"))
                .put()
                .uri("/api/v1/police-departments/{id}", 1)
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
                .uri("/api/v1/police-departments/{id}", 1)
                .exchange()
                .expectStatus().isForbidden();
    }
}