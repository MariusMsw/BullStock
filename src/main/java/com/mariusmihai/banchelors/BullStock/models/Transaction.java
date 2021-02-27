package com.mariusmihai.banchelors.BullStock.models;

import com.mariusmihai.banchelors.BullStock.utils.Currency;
import com.mariusmihai.banchelors.BullStock.utils.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.persistence.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Accessors(chain = true)
@Entity
@Table(name = "transaction")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @OneToOne
    private Stock stock;
    private long openDate;
    private long closeDate;
    @Enumerated(value = EnumType.STRING)
    private TransactionType type;
    private int volume;
    private double closePrice;
    private double openPrice;
    @Enumerated(value = EnumType.STRING)
    private Currency currency;
    private double exchangeRate;
}
