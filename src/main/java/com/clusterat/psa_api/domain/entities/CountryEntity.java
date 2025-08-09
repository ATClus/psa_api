package com.clusterat.psa_api.domain.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.Contract;

@Entity
@Table(name = "countries")
@Data
@NoArgsConstructor
public class CountryEntity {
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
    private String isoCode;

    @Contract(pure = true)
    private CountryEntity(String name, String shortName, String isoCode) {
        this.name = name;
        this.shortName = shortName;
        this.isoCode = isoCode;
    }

    public static @org.jetbrains.annotations.NotNull CountryEntity create(String name, String shortName, String isoCode) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be null or empty");
        }
        if (shortName == null || shortName.trim().isEmpty()) {
            throw new IllegalArgumentException("Short name cannot be null or empty");
        }
        if (isoCode == null || isoCode.trim().isEmpty()) {
            throw new IllegalArgumentException("ISO code cannot be null or empty");
        }
        return new CountryEntity(name.trim(), shortName.trim(), isoCode.trim());
    }
}
