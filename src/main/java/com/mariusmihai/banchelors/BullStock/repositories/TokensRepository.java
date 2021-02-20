package com.mariusmihai.banchelors.BullStock.repositories;

import com.mariusmihai.banchelors.BullStock.models.UserTokens;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TokensRepository extends CrudRepository<UserTokens, Long> {

    Optional<UserTokens> findByUserId(Long userId);

    Integer deleteByUserId(Long userId);
}
