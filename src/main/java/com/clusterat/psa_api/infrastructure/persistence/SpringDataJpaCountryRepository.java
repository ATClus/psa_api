package com.clusterat.psa_api.infrastructure.persistence;

import com.clusterat.psa_api.domain.entities.CountryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SpringDataJpaCountryRepository extends JpaRepository<CountryEntity, Integer> {

    @Query("SELECT c FROM CountryEntity c WHERE c.isoCode = :isoCode")
    Optional<CountryEntity> findByIsoCode(@Param("isoCode") String isoCode);
}