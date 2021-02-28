package com.mariusmihai.banchelors.BullStock.repositories;

import com.mariusmihai.banchelors.BullStock.models.FxRate;
import com.mariusmihai.banchelors.BullStock.utils.Currency;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FxRateRepository extends CrudRepository<FxRate, Integer> {

    void deleteByBaseCurrencyAndToCurrency(Currency baseCurrency, Currency toCurrency);

}
