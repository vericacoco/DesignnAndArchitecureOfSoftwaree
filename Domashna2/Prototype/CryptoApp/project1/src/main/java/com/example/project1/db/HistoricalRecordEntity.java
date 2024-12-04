package com.example.project1.db;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "historical_data")
@Data
@NoArgsConstructor
public class HistoricalRecordEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "date")
    private LocalDate date;

    @Column(name = "last_transaction_price")
    private Double lastTransactionPrice;

    @Column(name = "max_price")
    private Double maxPrice;

    @Column(name = "min_price")
    private Double minPrice;

    @Column(name = "average_price")
    private Double averagePrice;

    @Column(name = "percentage_change")
    private Double percentageChange;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "turnorver_best")
    private Integer turnoverBest;

    @Column(name = "total_turnover")
    private Integer totalTurnover;

    @ManyToOne
    @JoinColumn(name = "company_id")
    private CorporationEntity company;

    public HistoricalRecordEntity(LocalDate date, Double lastTransactionPrice, Double maxPrice, Double minPrice, Double averagePrice, Double percentageChange, Integer quantity, Integer turnoverBest, Integer totalTurnover) {
        this.date = date;
        this.lastTransactionPrice = lastTransactionPrice;
        this.maxPrice = maxPrice;
        this.minPrice = minPrice;
        this.averagePrice = averagePrice;
        this.percentageChange = percentageChange;
        this.quantity = quantity;
        this.turnoverBest = turnoverBest;
        this.totalTurnover = totalTurnover;
    }

}
