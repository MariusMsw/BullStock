package com.mariusmihai.banchelors.BullStock.dtos.stocks;

import com.mariusmihai.banchelors.BullStock.utils.StockChartPeriod;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class StockChartRequest {

    private String symbol;
    private StockChartPeriod period;
}
