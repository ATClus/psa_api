package com.clusterat.psa_api.presentation;

import com.clusterat.psa_api.application.dto.CountryApplicationDTO;
import com.clusterat.psa_api.config.TestSecurityConfig;
import com.clusterat.psa_api.presentation.dto.CountryPresentationDTO;
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
class CountryEndpointsIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ObjectMapper objectMapper;

    private static Integer createdCountryId;

    @BeforeEach
    void setUp() {
        webTestClient = webTestClient.mutate()
                .responseTimeout(Duration.ofSeconds(10))
                .build();
    }

    @Test
    @Order(1)
    void shouldCreateCountry() throws Exception {
        // Given
        CountryPresentationDTO.CreateRequest createRequest = new CountryPresentationDTO.CreateRequest(
                "Test Country",
                "TC",
                "TST"
        );

        // When & Then
        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockUser("testadmin").roles("ADMIN"))
                .post()
                .uri("/api/v1/countries")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(createRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(CountryApplicationDTO.Response.class)
                .value(response -> {
                    assertThat(response).isNotNull();
                    assertThat(response.id()).isNotNull();
                    assertThat(response.name()).isEqualTo("Test Country");
                    assertThat(response.shortName()).isEqualTo("TC");
                    assertThat(response.isoCode()).isEqualTo("TST");
                    createdCountryId = response.id();
                });
    }

    @Test
    @Order(2)
    void shouldGetAllCountries() {
        // When & Then
        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockUser("testuser").roles("USER"))
                .get()
                .uri("/api/v1/countries")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(CountryApplicationDTO.Response.class)
                .value(countries -> {
                    assertThat(countries).isNotEmpty();
                    assertThat(countries.get(0).name()).isNotNull();
                });
    }

    @Test
    @Order(3)
    void shouldGetCountryById() {
        // When & Then
        if (createdCountryId != null) {
            webTestClient
                    .mutateWith(SecurityMockServerConfigurers.mockUser("testuser").roles("USER"))
                    .get()
                    .uri("/api/v1/countries/{id}", createdCountryId)
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isOk()
                    .expectHeader().contentType(MediaType.APPLICATION_JSON)
                    .expectBody(CountryApplicationDTO.Response.class)
                    .value(country -> {
                        assertThat(country).isNotNull();
                        assertThat(country.id()).isEqualTo(createdCountryId);
                        assertThat(country.name()).isEqualTo("Test Country");
                        assertThat(country.isoCode()).isEqualTo("TST");
                    });
        }
    }

    @Test
    @Order(4)
    void shouldGetCountryByIsoCode() {
        // When & Then
        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockUser("testuser").roles("USER"))
                .get()
                .uri("/api/v1/countries/iso/{isoCode}", "TST")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(CountryApplicationDTO.Response.class)
                .value(country -> {
                    assertThat(country).isNotNull();
                    assertThat(country.isoCode()).isEqualTo("TST");
                    assertThat(country.name()).isEqualTo("Test Country");
                });
    }

    @Test
    @Order(5)
    void shouldUpdateCountry() throws Exception {
        // Given
        CountryPresentationDTO.UpdateRequest updateRequest = new CountryPresentationDTO.UpdateRequest(
                "Updated Test Country",
                "UTC",
                "UTS"
        );

        // When & Then
        if (createdCountryId != null) {
            webTestClient
                    .mutateWith(SecurityMockServerConfigurers.mockUser("testadmin").roles("ADMIN"))
                    .put()
                    .uri("/api/v1/countries/{id}", createdCountryId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(updateRequest)
                    .exchange()
                    .expectStatus().isOk()
                    .expectHeader().contentType(MediaType.APPLICATION_JSON)
                    .expectBody(CountryApplicationDTO.Response.class)
                    .value(country -> {
                        assertThat(country).isNotNull();
                        assertThat(country.id()).isEqualTo(createdCountryId);
                        assertThat(country.name()).isEqualTo("Updated Test Country");
                        assertThat(country.shortName()).isEqualTo("UTC");
                        assertThat(country.isoCode()).isEqualTo("UTS");
                    });
        }
    }

    @Test
    @Order(6)
    void shouldDeleteCountry() {
        // When & Then
        if (createdCountryId != null) {
            webTestClient
                    .mutateWith(SecurityMockServerConfigurers.mockUser("testadmin").roles("ADMIN"))
                    .delete()
                    .uri("/api/v1/countries/{id}", createdCountryId)
                    .exchange()
                    .expectStatus().isNoContent()
                    .expectBody().isEmpty();
        }
    }

    @Test
    void shouldReturnNotFoundForNonExistentCountry() {
        // When & Then
        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockUser("testuser").roles("USER"))
                .get()
                .uri("/api/v1/countries/{id}", 99999)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void shouldReturnNotFoundForNonExistentIsoCode() {
        // When & Then
        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockUser("testuser").roles("USER"))
                .get()
                .uri("/api/v1/countries/iso/{isoCode}", "XXX")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void shouldReturnBadRequestForInvalidCountryData() throws Exception {
        // Given - Invalid data (empty name)
        CountryPresentationDTO.CreateRequest invalidRequest = new CountryPresentationDTO.CreateRequest(
                "",  // Empty name
                "TC",
                "TST"
        );

        // When & Then
        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockUser("testadmin").roles("ADMIN"))
                .post()
                .uri("/api/v1/countries")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidRequest)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void shouldReturnBadRequestForNullFields() throws Exception {
        // Given - Invalid data (null name)
        CountryPresentationDTO.CreateRequest invalidRequest = new CountryPresentationDTO.CreateRequest(
                null,  // Null name
                "TC",
                "TST"
        );

        // When & Then
        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockUser("testadmin").roles("ADMIN"))
                .post()
                .uri("/api/v1/countries")
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
                .uri("/api/v1/countries")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void shouldAllowBasicAuthentication() {
        // When & Then - Basic authentication
        webTestClient
                .get()
                .uri("/api/v1/countries")
                .accept(MediaType.APPLICATION_JSON)
                .headers(headers -> headers.setBasicAuth("testuser", "testpassword"))
                .exchange()
                .expectStatus().isOk();
    }
}