package com.clusterat.psa_api.infrastructure.persistence;

import com.clusterat.psa_api.domain.entities.CityEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SpringDataJpaCityRepository extends JpaRepository<CityEntity, Integer> {

    @Query("SELECT c FROM CityEntity c WHERE c.ibgeCode = :ibgeCode")
    Optional<CityEntity> findByIbgeCode(@Param("ibgeCode") String ibgeCode);
}