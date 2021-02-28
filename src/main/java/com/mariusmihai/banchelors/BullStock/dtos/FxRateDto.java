package com.mariusmihai.banchelors.BullStock.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.LinkedHashMap;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Accessors(chain = true)
public class FxRateDto {

    private LinkedHashMap<String, Double> rates;
    private String base;
    private String date;
}
