package com.example.project1.data;

import com.example.project1.data.pipeline.Pipe;
import com.example.project1.data.pipeline.impl.CorporationFilter;
import com.example.project1.data.pipeline.impl.HistoricalRecordFilter;
import com.example.project1.data.pipeline.impl.HistoricalRecordReAddingFilter;
import com.example.project1.db.CorporationEntity;
import com.example.project1.repository.CorporationRepository;
import com.example.project1.repository.HistoricalRecordRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class StarterFilter {

    private final CorporationRepository corporationRepository;
    private final HistoricalRecordRepository historicalRecordRepository;

    @PostConstruct
    private void initializeData() throws IOException, ParseException {
        long startTime = System.nanoTime();

        Pipe<List<CorporationEntity>> pipe = new Pipe<>();
        pipe.addFilter(new CorporationFilter(corporationRepository));
        pipe.addFilter(new HistoricalRecordFilter(corporationRepository, historicalRecordRepository));
        pipe.addFilter(new HistoricalRecordReAddingFilter(corporationRepository, historicalRecordRepository));
        pipe.runFilter(null);

        long endTime = System.nanoTime();
        long durationInMillis = (endTime - startTime) / 1_000_000;

        long hours = durationInMillis / 3_600_000;
        long minutes = (durationInMillis % 3_600_000) / 60_000;
        long seconds = (durationInMillis % 60_000) / 1_000;

        System.out.printf("Total time for all filters to complete: %02d hours, %02d minutes, %02d seconds%n", hours, minutes, seconds);
    }

}
