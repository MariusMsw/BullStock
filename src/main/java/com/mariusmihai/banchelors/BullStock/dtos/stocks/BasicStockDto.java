package com.mariusmihai.banchelors.BullStock.dtos.stocks;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Accessors(chain = true)
public class BasicStockDto {
    private int id;
    private String name;
    private String symbol;
    private double bid;
    private double ask;
    private double priceChangeLastDay;
    private boolean isFavorite;
}
