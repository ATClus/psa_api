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

    public static CityEntity create(String name, String shortName, String ibgeCode, StateEntity state) {
        return new CityEntity(name, shortName, ibgeCode, state);
    }
}
