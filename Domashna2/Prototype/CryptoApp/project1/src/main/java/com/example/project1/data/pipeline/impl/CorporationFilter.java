package com.example.project1.data.pipeline.impl;

import com.example.project1.data.pipeline.Filter;
import com.example.project1.db.CorporationEntity;
import com.example.project1.repository.CorporationRepository;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.List;

public class CorporationFilter implements Filter<List<CorporationEntity>> {

    private final CorporationRepository corporationRepository;

    public CorporationFilter(CorporationRepository corporationRepository) {
        this.corporationRepository = corporationRepository;
    }

    private static final String STOCK_MARKET_URL = "https://www.mse.mk/mk/stats/symbolhistory/kmb";

    @Override
    public List<CorporationEntity> execute(List<CorporationEntity> input) throws IOException {
        Document document = Jsoup.connect(STOCK_MARKET_URL).get();
        Element selectMenu = document.select("select#Code").first();

        if (selectMenu != null) {
            Elements options = selectMenu.select("option");
            for (Element option : options) {
                String code = option.attr("value");
                if (!code.isEmpty() && code.matches("^[a-zA-Z]+$")) {
                    if (corporationRepository.findByCompanyCode(code).isEmpty()) {
                        corporationRepository.save(new CorporationEntity(code));
                    }
                }
            }
        }

        return corporationRepository.findAll();
    }
}
