package com.example.wallet.repository;

import com.example.wallet.model.implementations.Card;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CardRepository extends JpaRepository<Card, Long> {
    Optional<Card> findByClientEmail(String email);
}
