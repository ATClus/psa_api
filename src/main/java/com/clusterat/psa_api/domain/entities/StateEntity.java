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

    public static StateEntity create(String name, String shortName, Region region, String ibgeCode, CountryEntity country) {
        return new StateEntity(name, shortName, region, ibgeCode, country);
    }
}
