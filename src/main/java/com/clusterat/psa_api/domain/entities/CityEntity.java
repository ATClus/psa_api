package com.clusterat.psa_api.domain.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.Contract;

@Entity
@Table(name = "cities")
@Data
@NoArgsConstructor
public class CityEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @NotNull
    @Column(nullable = false)
    private String name;

    @NotNull
    @Column(nullable = false)
    private String shortName;

    @NotNull
    @Column(nullable = false)
    private String ibgeCode;

    @ManyToOne
    @JoinColumn(name = "state_id", nullable = false)
    private StateEntity state;

    @Contract(pure = true)
    private CityEntity(String name, String shortName, String ibgeCode, StateEntity state) {
        this.name = name;
        this.shortName = shortName;
        this.ibgeCode = ibgeCode;
        this.state = state;
    }

    public static @org.jetbrains.annotations.NotNull CityEntity create(String name, String shortName, String ibgeCode, StateEntity state) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be null or empty");
        }
        if (shortName == null || shortName.trim().isEmpty()) {
            throw new IllegalArgumentException("Short name cannot be null or empty");
        }
        if (ibgeCode == null || ibgeCode.trim().isEmpty()) {
            throw new IllegalArgumentException("IBGE code cannot be null or empty");
        }
        if (state == null) {
            throw new IllegalArgumentException("State cannot be null");
        }
        return new CityEntity(name.trim(), shortName.trim(), ibgeCode.trim(), state);
    }
}
