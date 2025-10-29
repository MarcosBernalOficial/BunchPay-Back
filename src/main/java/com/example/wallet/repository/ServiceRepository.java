package com.example.wallet.repository;

import com.example.wallet.model.implementations.Service;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceRepository extends JpaRepository<Service, Long> {
}
