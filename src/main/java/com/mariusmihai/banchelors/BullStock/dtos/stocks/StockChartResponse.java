package com.mariusmihai.banchelors.BullStock.dtos.stocks;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.Instant;

@Data
@Accessors(chain = true)
public class StockChartResponse {

    private Instant period;
    private double price;
}
