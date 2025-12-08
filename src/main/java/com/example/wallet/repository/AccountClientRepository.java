package com.example.wallet.repository;

import com.example.wallet.model.implementations.AccountClient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AccountClientRepository extends JpaRepository<AccountClient, Long> {
    Optional<AccountClient> findByClientEmail(String email);

    @Query("SELECT ac FROM AccountClient ac LEFT JOIN FETCH ac.transactionsList WHERE ac.client.email = :email")
    Optional<AccountClient> findByClientEmailWithTransactions(@Param("email") String email);

    boolean existsByAlias(String alias);

    Optional<AccountClient> findByCvu(String cvu);

    Optional<AccountClient> findByAlias(String alias);
}
