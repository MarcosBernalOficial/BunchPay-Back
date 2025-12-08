package com.example.wallet.repository;

import com.example.wallet.model.implementations.CryptoHolding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CryptoHoldingRepository extends JpaRepository<CryptoHolding, Long> {

    List<CryptoHolding> findByAccountClientId(Long accountClientId);

    Optional<CryptoHolding> findByAccountClientIdAndSymbol(Long accountClientId, String symbol);
}
