package com.mariusmihai.banchelors.BullStock.repositories;

import com.mariusmihai.banchelors.BullStock.models.Transaction;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends CrudRepository<Transaction, Integer> {

}
