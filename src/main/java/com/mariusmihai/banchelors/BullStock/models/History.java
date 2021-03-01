package com.mariusmihai.banchelors.BullStock.models;

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
@Table(name = "history")
public class History {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String symbol;
    private int userId;
    private int volume;
    private int transactionId;
    private TransactionType type;
    private Long openDate;
    private Long closeDate;
    private double profit;
    private double openPrice;
    private double closePrice;

}
