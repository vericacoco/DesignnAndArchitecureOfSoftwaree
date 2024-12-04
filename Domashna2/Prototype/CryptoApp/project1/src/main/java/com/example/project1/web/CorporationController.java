package com.example.project1.web;

import com.example.project1.db.CorporationEntity;
import com.example.project1.db.HistoricalRecordEntity;
import com.example.project1.service.CorporationService;
import com.example.project1.service.LSTM;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class CorporationController {

    private final CorporationService corporationService;
    private final LSTM LSTM;

    @GetMapping("/")
    public String getIndexPage(Model model) {
        model.addAttribute("companies", corporationService.findAll());
        return "index";
    }

    @GetMapping("/today")
    public String getTodayCompanyPage(Model model) {
        model.addAttribute("prices", corporationService.findAllToday());
        return "today";
    }

    @GetMapping("/company")
    public String getCompanyPage(@RequestParam(name = "companyId") Long companyId, Model model) throws Exception {
        List<Map<String, Object>> companyData = new ArrayList<>();
        CorporationEntity company = corporationService.findById(companyId);

        Map<String, Object> data = new HashMap<>();
        data.put("companyCode", company.getCompanyCode());
        data.put("lastUpdated", company.getLastUpdated());

        List<LocalDate> dates = new ArrayList<>();
        List<Double> prices = new ArrayList<>();

        for (HistoricalRecordEntity historicalData : company.getHistoricalData()) {
            dates.add(historicalData.getDate());
            prices.add(historicalData.getLastTransactionPrice());
        }

        data.put("dates", dates);
        data.put("prices", prices);
        data.put("id", company.getId());
        companyData.add(data);

        model.addAttribute("companyData", companyData);
        return "company";
    }

}
