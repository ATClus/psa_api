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

    public static CountryEntity create(String name, String shortName, String isoCode) {
        return new CountryEntity(name, shortName, isoCode);
    }
}
