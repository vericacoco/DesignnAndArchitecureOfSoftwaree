package com.example.project1.repository;

import com.example.project1.db.CorporationEntity;
import com.example.project1.db.HistoricalRecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface HistoricalRecordRepository extends JpaRepository<HistoricalRecordEntity, Long> {
    Optional<HistoricalRecordEntity> findByDateAndCompany(LocalDate date, CorporationEntity company);
    List<HistoricalRecordEntity> findByCompanyIdAndDateBetween(Long companyId, LocalDate from, LocalDate to);
    List<HistoricalRecordEntity> findAllByDate(LocalDate date);
}
