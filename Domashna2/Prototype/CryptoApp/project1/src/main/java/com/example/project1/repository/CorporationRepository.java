package com.example.project1.repository;

import com.example.project1.db.CorporationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CorporationRepository extends JpaRepository<CorporationEntity, Long> {
    Optional<CorporationEntity> findByCompanyCode(String companyCode);
}
