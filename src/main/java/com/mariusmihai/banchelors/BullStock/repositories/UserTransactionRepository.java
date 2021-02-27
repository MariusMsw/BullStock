package com.mariusmihai.banchelors.BullStock.repositories;

import com.mariusmihai.banchelors.BullStock.models.UserTransaction;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserTransactionRepository extends CrudRepository<UserTransaction, Integer> {

    @Query("SELECT t FROM UserTransaction t WHERE t.user.email = :email")
    List<UserTransaction> getAllUserTransactions(@Param("email") String email);
}
