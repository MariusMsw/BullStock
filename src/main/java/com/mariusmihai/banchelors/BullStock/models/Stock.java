package com.mariusmihai.banchelors.BullStock.models;

import com.mariusmihai.banchelors.BullStock.utils.Currency;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.persistence.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Accessors(chain = true)
@Entity
@Table(name = "stock")
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String name;
    private String symbol;
    private double bid;
    private double ask;
    private double closingPrice;
    private double openPrice;
    private String description;
    private String country;
    private String exchange;
    private String industry;
    private double marketCap;
    private double priceChangeLastDay;
    @Enumerated(EnumType.STRING)
    private Currency currency;
}
