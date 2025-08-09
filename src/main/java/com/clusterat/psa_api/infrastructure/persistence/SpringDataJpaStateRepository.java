package com.clusterat.psa_api.infrastructure.persistence;

import com.clusterat.psa_api.domain.entities.StateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SpringDataJpaStateRepository extends JpaRepository<StateEntity, Integer> {

    @Query("SELECT s FROM StateEntity s WHERE s.ibgeCode = :ibgeCode")
    Optional<StateEntity> findByIbgeCode(@Param("ibgeCode") String ibgeCode);
}