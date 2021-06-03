package com.mariusmihai.banchelors.BullStock.repositories;

import com.mariusmihai.banchelors.BullStock.models.UserTokens;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TokensRepository extends CrudRepository<UserTokens, Long> {

    List<UserTokens> findAllByUserId(int userId);

    Optional<UserTokens> findByAccessToken(String accessToken);

    void deleteByAccessToken(String accessToken);

    @Modifying
    @Query("delete from UserTokens ut where ut.user.id = :userId")
    Integer deleteAllByUserId(@Param("userId") int userId);
}
