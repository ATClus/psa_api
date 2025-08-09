package com.clusterat.psa_api.domain.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.Contract;

@Entity
@Table(name = "police_departments")
@Data
@NoArgsConstructor
public class PoliceDepartmentEntity {
   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private int id;

    @NotNull
    @Column(nullable = false)
    private String overpassId;

    @NotNull
    @Column(nullable = false)
    private String name;

    @NotNull
    @Column(nullable = false)
    private String shortName;

    @NotNull
    @Column(nullable = false)
    private String operator;

    @NotNull
    @Column(nullable = false)
    private String ownership;

    @NotNull
    @Column(nullable = false)
    private String phone;

    @NotNull
    @Column(nullable = false)
    private String email;

    @NotNull
    @Column(nullable = false)
    private String latitude;

    @NotNull
    @Column(nullable = false)
    private String longitude;

    @ManyToOne
    @JoinColumn(name = "address_id", nullable = false)
    private AddressEntity address;

    @Contract(pure = true)
    private PoliceDepartmentEntity(String overpassId, String name, String shortName, String operator, String ownership, String phone, String email, String latitude, String longitude, AddressEntity address) {
        this.overpassId = overpassId;
        this.name = name;
        this.shortName = shortName;
        this.operator = operator;
        this.ownership = ownership;
        this.phone = phone;
        this.email = email;
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
    }

    public static @org.jetbrains.annotations.NotNull PoliceDepartmentEntity create(String overpassId, String name, String shortName, String operator, String ownership, String phone, String email, String latitude, String longitude, AddressEntity address) {
        if (overpassId == null || overpassId.trim().isEmpty()) {
            throw new IllegalArgumentException("Overpass ID cannot be null or empty");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be null or empty");
        }
        if (shortName == null || shortName.trim().isEmpty()) {
            throw new IllegalArgumentException("Short name cannot be null or empty");
        }
        if (operator == null || operator.trim().isEmpty()) {
            throw new IllegalArgumentException("Operator cannot be null or empty");
        }
        if (ownership == null || ownership.trim().isEmpty()) {
            throw new IllegalArgumentException("Ownership cannot be null or empty");
        }
        if (phone == null || phone.trim().isEmpty()) {
            throw new IllegalArgumentException("Phone cannot be null or empty");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }
        if (latitude == null || latitude.trim().isEmpty()) {
            throw new IllegalArgumentException("Latitude cannot be null or empty");
        }
        if (longitude == null || longitude.trim().isEmpty()) {
            throw new IllegalArgumentException("Longitude cannot be null or empty");
        }
        if (address == null) {
            throw new IllegalArgumentException("Address cannot be null");
        }
        return new PoliceDepartmentEntity(overpassId.trim(), name.trim(), shortName.trim(), operator.trim(), ownership.trim(), phone.trim(), email.trim(), latitude.trim(), longitude.trim(), address);
    }
}
