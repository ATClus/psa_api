package com.clusterat.psa_api.infrastructure.persistence;

import com.clusterat.psa_api.domain.entities.AddressEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataJpaAddressRepository extends JpaRepository<AddressEntity, Integer> {
}