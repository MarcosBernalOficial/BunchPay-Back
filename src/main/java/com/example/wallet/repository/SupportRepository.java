package com.example.wallet.repository;

import com.example.wallet.model.implementations.Support;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SupportRepository extends JpaRepository<Support, Long> {
    Optional<Support> findByEmail(String email);
}
