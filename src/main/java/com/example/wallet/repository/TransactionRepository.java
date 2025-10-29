package com.example.wallet.repository;

import com.example.wallet.model.implementations.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
}
