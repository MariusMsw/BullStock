package com.mariusmihai.banchelors.BullStock.repositories;

import com.mariusmihai.banchelors.BullStock.models.Stock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockRepository extends CrudRepository<Stock, Integer> {

    @Query("SELECT s FROM Stock s ORDER BY s.priceChangeLastDay DESC")
    List<Stock> getWinners();

    @Query("SELECT s FROM Stock s ORDER BY s.priceChangeLastDay ASC")
    List<Stock> getLosers();

    @Query("SELECT s FROM Stock s ORDER BY s.symbol ASC")
    List<Stock> findAllStocks();
}
