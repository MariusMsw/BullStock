package com.mariusmihai.banchelors.BullStock.dtos.stocks;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class PortfolioScreenDto {

    private String symbol;
    private Integer sharesOwned;
    private Double profit;
    private Double yield;
}
