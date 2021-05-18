package com.mariusmihai.banchelors.BullStock.dtos.stocks;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class StockScreenDto {

    private List<StockChartResponse> data;
    private boolean favorite;
    private String stockName;
    private Integer sharesOwned;
    private Double sharePrice;
}
