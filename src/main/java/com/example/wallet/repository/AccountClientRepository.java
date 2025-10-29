package com.example.wallet.repository;

import com.example.wallet.model.implementations.AccountClient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountClientRepository extends JpaRepository<AccountClient, Long> {
    Optional<AccountClient> findByClientEmail(String email);

    boolean existsByAlias(String alias);

    Optional<AccountClient> findByCvu(String cvu);

    Optional<AccountClient> findByAlias(String alias);
}
