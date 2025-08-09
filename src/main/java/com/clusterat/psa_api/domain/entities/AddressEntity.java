package com.clusterat.psa_api.domain.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.Contract;

@Entity
@Table(name = "addresses")
@Data
@NoArgsConstructor
public class AddressEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @NotNull
    @Column(nullable = false)
    private String street;

    @NotNull
    @Column(nullable = false)
    private String number;

    @NotNull
    @Column(nullable = false)
    private String complement;

    @NotNull
    @Column(nullable = false)
    private String neighborhood;

    @ManyToOne
    @JoinColumn(name = "city_id", nullable = false)
    private CityEntity city;

    @Contract(pure = true)
    private AddressEntity(String street, String number, String complement, String neighborhood, CityEntity city) {
        this.street = street;
        this.number = number;
        this.complement = complement;
        this.neighborhood = neighborhood;
        this.city = city;
    }

    public static @org.jetbrains.annotations.NotNull AddressEntity create(String street, String number, String complement, String neighborhood, CityEntity city) {
        if (street == null || street.trim().isEmpty()) {
            throw new IllegalArgumentException("Street cannot be null or empty");
        }
        if (number == null || number.trim().isEmpty()) {
            throw new IllegalArgumentException("Number cannot be null or empty");
        }
        if (complement == null || complement.trim().isEmpty()) {
            throw new IllegalArgumentException("Complement cannot be null or empty");
        }
        if (neighborhood == null || neighborhood.trim().isEmpty()) {
            throw new IllegalArgumentException("Neighborhood cannot be null or empty");
        }
        if (city == null) {
            throw new IllegalArgumentException("City cannot be null");
        }
        return new AddressEntity(street.trim(), number.trim(), complement.trim(), neighborhood.trim(), city);
    }
}
