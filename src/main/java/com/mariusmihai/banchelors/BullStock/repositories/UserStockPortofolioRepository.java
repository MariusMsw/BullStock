package com.mariusmihai.banchelors.BullStock.repositories;

import com.mariusmihai.banchelors.BullStock.models.UserStockPortofolio;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserStockPortofolioRepository extends CrudRepository<UserStockPortofolio, Integer> {

    @Query("SELECT s FROM UserStockPortofolio s WHERE s.user.id = :id ORDER BY s.stock.symbol")
    List<UserStockPortofolio> getPortofolio(@Param("id") int userId);

    @Query("SELECT s.volume FROM UserStockPortofolio s WHERE s.stock.symbol = :symbol AND s.user.id = :userId")
    Integer findVolumeBySymbol(@Param("symbol") String symbol, @Param("userId") Integer userId);

}
