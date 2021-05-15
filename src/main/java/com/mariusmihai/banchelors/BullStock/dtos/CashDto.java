package com.mariusmihai.banchelors.BullStock.dtos;

import com.mariusmihai.banchelors.BullStock.utils.CashOperationType;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class CashDto {

    private double amount;

}
