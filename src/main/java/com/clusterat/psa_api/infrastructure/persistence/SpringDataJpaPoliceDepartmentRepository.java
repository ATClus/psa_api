package com.clusterat.psa_api.infrastructure.persistence;

import com.clusterat.psa_api.domain.entities.PoliceDepartmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SpringDataJpaPoliceDepartmentRepository extends JpaRepository<PoliceDepartmentEntity, Integer> {

    @Query("SELECT p FROM PoliceDepartmentEntity p WHERE p.overpassId = :overpassId")
    Optional<PoliceDepartmentEntity> findByOverpassId(@Param("overpassId") String overpassId);
}