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

    public static AddressEntity create(String street, String number, String complement, String neighborhood, CityEntity city) {
        return new AddressEntity(street, number, complement, neighborhood, city);
    }
}
