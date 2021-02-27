package com.mariusmihai.banchelors.BullStock.repositories;

import com.mariusmihai.banchelors.BullStock.models.UserStockPortofolio;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserStockPortofolioRepository extends CrudRepository<UserStockPortofolio, Integer> {

    @Query("SELECT s FROM UserStockPortofolio s ORDER BY s.stock.symbol")
    List<UserStockPortofolio> getPortofolio();

}
