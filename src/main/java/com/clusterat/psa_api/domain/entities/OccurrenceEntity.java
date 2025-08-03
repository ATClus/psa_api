package com.clusterat.psa_api.domain.entities;

import com.clusterat.psa_api.domain.value_objects.Intensity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.Contract;

import java.util.Date;

@Entity
@Table(name = "occurrences")
@Data
@NoArgsConstructor
public class OccurrenceEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @NotNull
    @Column(nullable = false)
    private String name;

    @NotNull
    @Column(nullable = false)
    private String description;

    @NotNull
    @Column(nullable = false)
    private Date dateStart;

    private Date dateEnd;
    private Date dateUpdate;

    @NotNull
    @Column(nullable = false)
    private boolean active;

    @NotNull
    @Column(nullable = false)
    private Intensity intensity;

    @ManyToOne
    @JoinColumn(name = "address_id", nullable = false)
    private AddressEntity address;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Contract(pure = true)
    private OccurrenceEntity(String name, String description, Date dateStart, Date dateEnd, Date dateUpdate, boolean active, Intensity intensity, AddressEntity address, UserEntity user) {
        this.name = name;
        this.description = description;
        this.dateStart = dateStart;
        this.dateEnd = dateEnd;
        this.dateUpdate = dateUpdate;
        this.active = active;
        this.intensity = intensity;
        this.address = address;
        this.user = user;
    }

    public static OccurrenceEntity create(String name, String description, Date dateStart, Date dateEnd, Date dateUpdate, boolean active, Intensity intensity, AddressEntity address, UserEntity user) {
        return new OccurrenceEntity(name, description, dateStart, dateEnd, dateUpdate, active, intensity, address, user);
    }
}
