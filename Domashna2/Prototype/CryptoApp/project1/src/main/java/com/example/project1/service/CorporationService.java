package com.example.project1.service;

import com.example.project1.db.CorporationEntity;
import com.example.project1.db.HistoricalRecordEntity;
import com.example.project1.repository.HistoricalRecordRepository;
import com.example.project1.repository.CorporationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CorporationService {

    private final CorporationRepository corporationRepository;
    private final HistoricalRecordRepository historicalRecordRepository;

    public List<CorporationEntity> findAll() {
        return corporationRepository.findAll();
    }

    public CorporationEntity findById(Long id) throws Exception {
        return corporationRepository.findById(id).orElseThrow(Exception::new);
    }

    public List<HistoricalRecordEntity> findAllToday() {
        return historicalRecordRepository.findAllByDate(LocalDate.now());
    }

}
