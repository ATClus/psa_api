package com.clusterat.psa_api.domain.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.Contract;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @NotNull
    @Column(nullable = false)
    private int cognitoId;

    @Contract(pure = true)
    private UserEntity(int cognitoId) {
        this.cognitoId = cognitoId;
    }

    public static @org.jetbrains.annotations.NotNull UserEntity create(int cognitoId) {
        if (cognitoId <= 0) {
            throw new IllegalArgumentException("Cognito ID must be positive");
        }
        return new UserEntity(cognitoId);
    }
}