package com.example.project1.data.pipeline.impl;

import com.example.project1.data.InformationTransformer;
import com.example.project1.data.pipeline.Filter;
import com.example.project1.db.CorporationEntity;
import com.example.project1.db.HistoricalRecordEntity;
import com.example.project1.repository.CorporationRepository;
import com.example.project1.repository.HistoricalRecordRepository;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HistoricalRecordFilter implements Filter<List<CorporationEntity>> {

    private final CorporationRepository corporationRepository;
    private final HistoricalRecordRepository historicalRecordRepository;

    private static final String HISTORICAL_DATA_URL = "https://www.mse.mk/mk/stats/symbolhistory/";

    public HistoricalRecordFilter(CorporationRepository corporationRepository, HistoricalRecordRepository historicalRecordRepository) {
        this.corporationRepository = corporationRepository;
        this.historicalRecordRepository = historicalRecordRepository;
    }

    public List<CorporationEntity> execute(List<CorporationEntity> input) throws IOException {
        List<CorporationEntity> companies = new ArrayList<>();

        for (CorporationEntity company : input) {
            if (company.getLastUpdated() == null) {
                for (int i = 1; i <= 10; i++) {
                    int temp = i - 1;
                    LocalDate fromDate = LocalDate.now().minusYears(i);
                    LocalDate toDate = LocalDate.now().minusYears(temp);
                    addHistoricalData(company, fromDate, toDate);
                }
            } else {
                companies.add(company);
            }
        }

        return companies;
    }

    private void addHistoricalData(CorporationEntity company, LocalDate fromDate, LocalDate toDate) throws IOException {
        Connection.Response response = Jsoup.connect(HISTORICAL_DATA_URL + company.getCompanyCode())
                .data("FromDate", fromDate.toString())
                .data("ToDate", toDate.toString())
                .method(Connection.Method.POST)
                .execute();

        Document document = response.parse();

        Element table = document.select("table#resultsTable").first();

        if (table != null) {
            Elements rows = table.select("tbody tr");

            for (Element row : rows) {
                Elements columns = row.select("td");

                if (columns.size() > 0) {
                    LocalDate date = InformationTransformer.parseDate(columns.get(0).text(), "d.M.yyyy");

                    if (historicalRecordRepository.findByDateAndCompany(date, company).isEmpty()) {


                        NumberFormat format = NumberFormat.getInstance(Locale.GERMANY);

                        Double lastTransactionPrice = InformationTransformer.parseDouble(columns.get(1).text(), format);
                        Double maxPrice = InformationTransformer.parseDouble(columns.get(2).text(), format);
                        Double minPrice = InformationTransformer.parseDouble(columns.get(3).text(), format);
                        Double averagePrice = InformationTransformer.parseDouble(columns.get(4).text(), format);
                        Double percentageChange = InformationTransformer.parseDouble(columns.get(5).text(), format);
                        Integer quantity = InformationTransformer.parseInteger(columns.get(6).text(), format);
                        Integer turnoverBest = InformationTransformer.parseInteger(columns.get(7).text(), format);
                        Integer totalTurnover = InformationTransformer.parseInteger(columns.get(8).text(), format);

                        if (maxPrice != null) {

                            if (company.getLastUpdated() == null || company.getLastUpdated().isBefore(date)) {
                                company.setLastUpdated(date);
                            }

                            HistoricalRecordEntity historicalRecordEntity = new HistoricalRecordEntity(
                                    date, lastTransactionPrice, maxPrice, minPrice, averagePrice, percentageChange,
                                    quantity, turnoverBest, totalTurnover);
                            historicalRecordEntity.setCompany(company);
                            historicalRecordRepository.save(historicalRecordEntity);
                            company.getHistoricalData().add(historicalRecordEntity);
                        }
                    }
                }
            }
        }

        corporationRepository.save(company);
    }

}
