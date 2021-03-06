package com.mariusmihai.banchelors.BullStock.models;

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
@Table(name = "user_stock_portofolio")
public class UserStockPortofolio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @OneToOne
    private Stock stock;
    @OneToOne
    private User user;
    private double averagePrice;
    private int volume;
    private double profit;
    private double yield;
}
