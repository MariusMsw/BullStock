package com.mariusmihai.banchelors.BullStock.dtos.stocks;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class PortfolioMetadataDto {

    private Double portfolioValue;
    private Double balance;
}
