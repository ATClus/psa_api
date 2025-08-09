package com.clusterat.psa_api.application.commands;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class CreteUserCommandTest {

    @Test
    void constructor_ShouldCreateCommand_WhenValidCognitoId() {
        // Given
        int cognitoId = 12345;

        // When
        CreteUserCommand command = new CreteUserCommand(cognitoId);

        // Then
        assertThat(command).isNotNull();
        assertThat(command.cognitoId()).isEqualTo(12345);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 100, 999999, Integer.MAX_VALUE})
    void constructor_ShouldCreateCommand_WhenValidCognitoIds(int cognitoId) {
        // When
        CreteUserCommand command = new CreteUserCommand(cognitoId);

        // Then
        assertThat(command).isNotNull();
        assertThat(command.cognitoId()).isEqualTo(cognitoId);
    }

    @Test
    void constructor_ShouldCreateCommand_WhenZeroCognitoId() {
        // Given - Note: Command itself doesn't validate, validation happens in UserEntity.create()
        int cognitoId = 0;

        // When
        CreteUserCommand command = new CreteUserCommand(cognitoId);

        // Then
        assertThat(command).isNotNull();
        assertThat(command.cognitoId()).isEqualTo(0);
    }

    @Test
    void constructor_ShouldCreateCommand_WhenNegativeCognitoId() {
        // Given - Note: Command itself doesn't validate, validation happens in UserEntity.create()
        int cognitoId = -1;

        // When
        CreteUserCommand command = new CreteUserCommand(cognitoId);

        // Then
        assertThat(command).isNotNull();
        assertThat(command.cognitoId()).isEqualTo(-1);
    }

    @Test
    void equals_ShouldReturnTrue_WhenSameObject() {
        // Given
        CreteUserCommand command = new CreteUserCommand(12345);

        // When & Then
        assertThat(command).isEqualTo(command);
    }

    @Test
    void equals_ShouldReturnTrue_WhenSameCognitoId() {
        // Given
        CreteUserCommand command1 = new CreteUserCommand(12345);
        CreteUserCommand command2 = new CreteUserCommand(12345);

        // When & Then
        assertThat(command1).isEqualTo(command2);
        assertThat(command1.hashCode()).isEqualTo(command2.hashCode());
    }

    @Test
    void equals_ShouldReturnFalse_WhenDifferentCognitoId() {
        // Given
        CreteUserCommand command1 = new CreteUserCommand(12345);
        CreteUserCommand command2 = new CreteUserCommand(54321);

        // When & Then
        assertThat(command1).isNotEqualTo(command2);
    }

    @Test
    void equals_ShouldReturnFalse_WhenNull() {
        // Given
        CreteUserCommand command = new CreteUserCommand(12345);

        // When & Then
        assertThat(command).isNotEqualTo(null);
    }

    @Test
    void equals_ShouldReturnFalse_WhenDifferentType() {
        // Given
        CreteUserCommand command = new CreteUserCommand(12345);
        String notACommand = "not a command";

        // When & Then
        assertThat(command).isNotEqualTo(notACommand);
    }

    @Test
    void toString_ShouldContainCognitoId() {
        // Given
        CreteUserCommand command = new CreteUserCommand(12345);

        // When
        String result = command.toString();

        // Then
        assertThat(result).contains("CreteUserCommand");
        assertThat(result).contains("12345");
    }

    @Test
    void hashCode_ShouldBeConsistent() {
        // Given
        CreteUserCommand command = new CreteUserCommand(12345);

        // When
        int hashCode1 = command.hashCode();
        int hashCode2 = command.hashCode();

        // Then
        assertThat(hashCode1).isEqualTo(hashCode2);
    }

    @Test
    void hashCode_ShouldBeSameForEqualCommands() {
        // Given
        CreteUserCommand command1 = new CreteUserCommand(12345);
        CreteUserCommand command2 = new CreteUserCommand(12345);

        // When & Then
        assertThat(command1.hashCode()).isEqualTo(command2.hashCode());
    }

    @Test
    void record_ShouldBeImmutable() {
        // Given
        CreteUserCommand command = new CreteUserCommand(12345);

        // When - Try to verify immutability (records are immutable by nature)
        int originalCognitoId = command.cognitoId();

        // Then - The cognitoId should remain the same
        assertThat(command.cognitoId()).isEqualTo(originalCognitoId);
        assertThat(command.cognitoId()).isEqualTo(12345);
    }
}