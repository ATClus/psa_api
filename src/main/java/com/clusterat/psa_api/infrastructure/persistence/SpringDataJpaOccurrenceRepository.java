package com.clusterat.psa_api.infrastructure.persistence;

import com.clusterat.psa_api.domain.entities.OccurrenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpringDataJpaOccurrenceRepository extends JpaRepository<OccurrenceEntity, Integer> {

    @Query("SELECT o FROM OccurrenceEntity o WHERE o.active = :active")
    List<OccurrenceEntity> findByActive(@Param("active") boolean active);
    
    @Query("SELECT o FROM OccurrenceEntity o WHERE o.user.id = :userId")
    List<OccurrenceEntity> findByUserId(@Param("userId") int userId);
}