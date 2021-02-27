package com.mariusmihai.banchelors.BullStock.dtos.stocks;

import com.mariusmihai.banchelors.BullStock.utils.TransactionType;
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
public class UserHistory {

    private String symbol;
    private int volume;
    private int transactionId;
    private TransactionType type;
    private Long openDate;
    private Long closeDate;
    private double profit;
    private double openPrice;
    private double closePrice;
}
