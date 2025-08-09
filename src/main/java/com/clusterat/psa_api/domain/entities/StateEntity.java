package com.clusterat.psa_api.domain.entities;

import com.clusterat.psa_api.domain.value_objects.Region;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.Contract;

@Entity
@Table(name = "states")
@Data
@NoArgsConstructor
public class StateEntity {
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
    private Region region;

    @NotNull
    @Column(nullable = false)
    private String ibgeCode;

    @ManyToOne
    @JoinColumn(name = "country_id", nullable = false)
    private CountryEntity country;

    @Contract(pure = true)
    private StateEntity(String name, String shortName, Region region, String ibgeCode, CountryEntity country) {
        this.name = name;
        this.shortName = shortName;
        this.region = region;
        this.ibgeCode = ibgeCode;
        this.country = country;
    }

    public static @org.jetbrains.annotations.NotNull StateEntity create(String name, String shortName, Region region, String ibgeCode, CountryEntity country) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be null or empty");
        }
        if (shortName == null || shortName.trim().isEmpty()) {
            throw new IllegalArgumentException("Short name cannot be null or empty");
        }
        if (region == null) {
            throw new IllegalArgumentException("Region cannot be null");
        }
        if (ibgeCode == null || ibgeCode.trim().isEmpty()) {
            throw new IllegalArgumentException("IBGE code cannot be null or empty");
        }
        if (country == null) {
            throw new IllegalArgumentException("Country cannot be null");
        }
        return new StateEntity(name.trim(), shortName.trim(), region, ibgeCode.trim(), country);
    }
}
