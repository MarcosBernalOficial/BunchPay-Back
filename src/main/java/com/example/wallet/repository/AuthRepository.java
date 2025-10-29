package com.example.wallet.repository;

import com.example.wallet.model.implementations.User;

import java.util.Optional;

public interface AuthRepository {
    Optional<User> findByEmail(String email);
}
