package com.mariusmihai.banchelors.BullStock.models;

import com.mariusmihai.banchelors.BullStock.utils.Currency;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Accessors(chain = true)
@Entity
@Table(name = "fx_rate")
public class FxRate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Enumerated(value = EnumType.STRING)
    private Currency baseCurrency;
    @Enumerated(value = EnumType.STRING)
    private Currency toCurrency;
    private double conversionRate;
    private Instant fetchTime;
}
