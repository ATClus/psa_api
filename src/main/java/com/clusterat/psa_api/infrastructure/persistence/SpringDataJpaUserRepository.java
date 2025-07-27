package com.clusterat.psa_api.infrastructure.persistence;

import com.clusterat.psa_api.domain.entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SpringDataJpaUserRepository extends JpaRepository<UserEntity, Integer> {

    @Query("SELECT u FROM UserEntity u WHERE u.cognitoId = :cognitoId")
    Optional<UserEntity> findByCognitoId(@Param("cognitoId") int cognitoId);
}
