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

    public static PoliceDepartmentEntity create(String overpassId, String name, String shortName, String operator, String ownership, String phone, String email, String latitude, String longitude, AddressEntity address) {
        return new PoliceDepartmentEntity(overpassId, name, shortName, operator, ownership, phone, email, latitude, longitude, address);
    }
}
