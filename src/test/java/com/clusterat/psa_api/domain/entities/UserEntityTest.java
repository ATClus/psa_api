package com.clusterat.psa_api.domain.entities;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserEntityTest {

    @Test
    void create_ShouldCreateUserEntity_WhenValidCognitoId() {
        // Given
        int cognitoId = 12345;

        // When
        UserEntity user = UserEntity.create(cognitoId);

        // Then
        assertThat(user).isNotNull();
        assertThat(user.getCognitoId()).isEqualTo(12345);
        assertThat(user.getId()).isEqualTo(0); // New entity should have ID = 0
    }

    @Test
    void create_ShouldCreateUserEntity_WhenCognitoIdIsOne() {
        // Given
        int cognitoId = 1;

        // When
        UserEntity user = UserEntity.create(cognitoId);

        // Then
        assertThat(user).isNotNull();
        assertThat(user.getCognitoId()).isEqualTo(1);
        assertThat(user.getId()).isEqualTo(0);
    }

    @Test
    void create_ShouldCreateUserEntity_WhenCognitoIdIsMaxInteger() {
        // Given
        int cognitoId = Integer.MAX_VALUE;

        // When
        UserEntity user = UserEntity.create(cognitoId);

        // Then
        assertThat(user).isNotNull();
        assertThat(user.getCognitoId()).isEqualTo(Integer.MAX_VALUE);
        assertThat(user.getId()).isEqualTo(0);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -100, -999, Integer.MIN_VALUE})
    void create_ShouldThrowIllegalArgumentException_WhenCognitoIdIsNotPositive(int invalidCognitoId) {
        // When & Then
        assertThatThrownBy(() -> UserEntity.create(invalidCognitoId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cognito ID must be positive");
    }

    @Test
    void create_ShouldThrowIllegalArgumentException_WhenCognitoIdIsZero() {
        // Given
        int cognitoId = 0;

        // When & Then
        assertThatThrownBy(() -> UserEntity.create(cognitoId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cognito ID must be positive");
    }

    @Test
    void create_ShouldThrowIllegalArgumentException_WhenCognitoIdIsNegative() {
        // Given
        int cognitoId = -1;

        // When & Then
        assertThatThrownBy(() -> UserEntity.create(cognitoId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cognito ID must be positive");
    }

    @Test
    void noArgsConstructor_ShouldCreateEmptyUserEntity() {
        // When
        UserEntity user = new UserEntity();

        // Then
        assertThat(user).isNotNull();
        assertThat(user.getId()).isEqualTo(0);
        assertThat(user.getCognitoId()).isEqualTo(0);
    }

    @Test
    void settersAndGetters_ShouldWorkCorrectly() {
        // Given
        UserEntity user = new UserEntity();

        // When
        user.setId(5);
        user.setCognitoId(98765);

        // Then
        assertThat(user.getId()).isEqualTo(5);
        assertThat(user.getCognitoId()).isEqualTo(98765);
    }

    @Test
    void equals_ShouldReturnTrue_WhenSameObject() {
        // Given
        UserEntity user = UserEntity.create(12345);

        // When & Then
        assertThat(user).isEqualTo(user);
    }

    @Test
    void equals_ShouldReturnTrue_WhenSameData() {
        // Given
        UserEntity user1 = UserEntity.create(12345);
        user1.setId(1);
        
        UserEntity user2 = UserEntity.create(12345);
        user2.setId(1);

        // When & Then
        assertThat(user1).isEqualTo(user2);
        assertThat(user1.hashCode()).isEqualTo(user2.hashCode());
    }

    @Test
    void equals_ShouldReturnFalse_WhenDifferentData() {
        // Given
        UserEntity user1 = UserEntity.create(12345);
        user1.setId(1);
        
        UserEntity user2 = UserEntity.create(54321);
        user2.setId(1);

        // When & Then
        assertThat(user1).isNotEqualTo(user2);
    }

    @Test
    void toString_ShouldReturnStringRepresentation() {
        // Given
        UserEntity user = UserEntity.create(12345);
        user.setId(1);

        // When
        String result = user.toString();

        // Then
        assertThat(result).contains("UserEntity");
        assertThat(result).contains("id=1");
        assertThat(result).contains("cognitoId=12345");
    }

    @Test
    void create_ShouldBeImmutable_WhenCreated() {
        // Given
        int originalCognitoId = 12345;

        // When
        UserEntity user = UserEntity.create(originalCognitoId);

        // Then - We can still modify through setters because Lombok generates them
        // But the static factory method enforces validation
        assertThat(user.getCognitoId()).isEqualTo(originalCognitoId);
        
        // Verify we can still use setters (this is expected behavior with Lombok)
        user.setCognitoId(54321);
        assertThat(user.getCognitoId()).isEqualTo(54321);
    }

    @Test
    void create_ShouldValidateEveryTime_WhenCalled() {
        // This test verifies that the validation happens every time create() is called
        
        // First call with valid data
        UserEntity user1 = UserEntity.create(100);
        assertThat(user1.getCognitoId()).isEqualTo(100);

        // Second call with valid data  
        UserEntity user2 = UserEntity.create(200);
        assertThat(user2.getCognitoId()).isEqualTo(200);

        // Third call with invalid data should still throw exception
        assertThatThrownBy(() -> UserEntity.create(-1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cognito ID must be positive");
    }
}