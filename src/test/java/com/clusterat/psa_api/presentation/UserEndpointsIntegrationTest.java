package com.clusterat.psa_api.presentation;

import com.clusterat.psa_api.application.dto.UserApplicationDTO;
import com.clusterat.psa_api.config.TestSecurityConfig;
import com.clusterat.psa_api.presentation.dto.UserPresentationDTO;
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
class UserEndpointsIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ObjectMapper objectMapper;

    private static Integer createdUserId;

    @BeforeEach
    void setUp() {
        webTestClient = webTestClient.mutate()
                .responseTimeout(Duration.ofSeconds(10))
                .build();
    }

    @Test
    @Order(1)
    void shouldCreateUser() throws Exception {
        // Given
        UserPresentationDTO.CreateRequest createRequest = new UserPresentationDTO.CreateRequest(12345);

        // When & Then
        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockUser("testadmin").roles("ADMIN"))
                .post()
                .uri("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(createRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(UserApplicationDTO.Response.class)
                .value(response -> {
                    assertThat(response).isNotNull();
                    assertThat(response.id()).isNotNull();
                    assertThat(response.cognitoId()).isEqualTo(12345);
                    createdUserId = response.id();
                });
    }

    @Test
    @Order(2)
    void shouldGetAllUsers() {
        // When & Then
        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockUser("testuser").roles("USER"))
                .get()
                .uri("/api/v1/users")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(UserApplicationDTO.Response.class)
                .value(users -> {
                    assertThat(users).isNotEmpty();
                    assertThat(users.get(0).cognitoId()).isNotNull();
                });
    }

    @Test
    @Order(3)
    void shouldGetUserById() {
        // When & Then
        if (createdUserId != null) {
            webTestClient
                    .mutateWith(SecurityMockServerConfigurers.mockUser("testuser").roles("USER"))
                    .get()
                    .uri("/api/v1/users/{id}", createdUserId)
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isOk()
                    .expectHeader().contentType(MediaType.APPLICATION_JSON)
                    .expectBody(UserApplicationDTO.Response.class)
                    .value(user -> {
                        assertThat(user).isNotNull();
                        assertThat(user.id()).isEqualTo(createdUserId);
                        assertThat(user.cognitoId()).isEqualTo(12345);
                    });
        }
    }

    @Test
    @Order(4)
    void shouldGetUserByCognitoId() {
        // When & Then
        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockUser("testuser").roles("USER"))
                .get()
                .uri("/api/v1/users/cognito/{cognitoId}", 12345)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(UserApplicationDTO.Response.class)
                .value(user -> {
                    assertThat(user).isNotNull();
                    assertThat(user.cognitoId()).isEqualTo(12345);
                });
    }

    @Test
    @Order(5)
    void shouldUpdateUser() throws Exception {
        // Given
        UserPresentationDTO.CreateRequest updateRequest = new UserPresentationDTO.CreateRequest(54321);

        // When & Then
        if (createdUserId != null) {
            webTestClient
                    .mutateWith(SecurityMockServerConfigurers.mockUser("testadmin").roles("ADMIN"))
                    .put()
                    .uri("/api/v1/users/{id}", createdUserId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(updateRequest)
                    .exchange()
                    .expectStatus().isOk()
                    .expectHeader().contentType(MediaType.APPLICATION_JSON)
                    .expectBody(UserApplicationDTO.Response.class)
                    .value(user -> {
                        assertThat(user).isNotNull();
                        assertThat(user.id()).isEqualTo(createdUserId);
                        assertThat(user.cognitoId()).isEqualTo(54321);
                    });
        }
    }

    @Test
    @Order(6)
    void shouldDeleteUser() {
        // When & Then
        if (createdUserId != null) {
            webTestClient
                    .mutateWith(SecurityMockServerConfigurers.mockUser("testadmin").roles("ADMIN"))
                    .delete()
                    .uri("/api/v1/users/{id}", createdUserId)
                    .exchange()
                    .expectStatus().isNoContent()
                    .expectBody().isEmpty();
        }
    }

    @Test
    void shouldReturnNotFoundForNonExistentUser() {
        // When & Then
        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockUser("testuser").roles("USER"))
                .get()
                .uri("/api/v1/users/{id}", 99999)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void shouldReturnBadRequestForInvalidUserData() throws Exception {
        // Given - Invalid cognito ID (negative)
        UserPresentationDTO.CreateRequest invalidRequest = new UserPresentationDTO.CreateRequest(-1);

        // When & Then
        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockUser("testadmin").roles("ADMIN"))
                .post()
                .uri("/api/v1/users")
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
                .uri("/api/v1/users")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void shouldAllowBasicAuthentication() {
        // When & Then - Basic authentication
        webTestClient
                .get()
                .uri("/api/v1/users")
                .accept(MediaType.APPLICATION_JSON)
                .headers(headers -> headers.setBasicAuth("testuser", "testpassword"))
                .exchange()
                .expectStatus().isOk();
    }
}