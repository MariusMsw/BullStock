package com.mariusmihai.banchelors.BullStock.repositories;

import com.mariusmihai.banchelors.BullStock.models.FxRate;
import com.mariusmihai.banchelors.BullStock.utils.Currency;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FxRateRepository extends CrudRepository<FxRate, Integer> {

    void deleteByBaseCurrencyAndToCurrency(Currency baseCurrency, Currency toCurrency);

    @Query("SELECT fx.conversionRate FROM FxRate fx WHERE fx.baseCurrency = :baseCurrency AND fx.toCurrency = :toCurrency")
    double findConversionRateByBaseCurrencyAndToCurrency(@Param("baseCurrency") Currency baseCurrency, @Param("toCurrency") Currency toCurrency);
}
