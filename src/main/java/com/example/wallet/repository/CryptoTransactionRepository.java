package com.example.wallet.repository;

import com.example.wallet.model.implementations.CryptoTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CryptoTransactionRepository extends JpaRepository<CryptoTransaction, Long> {

    List<CryptoTransaction> findByAccountClientIdOrderByDateDesc(Long accountClientId);
}
