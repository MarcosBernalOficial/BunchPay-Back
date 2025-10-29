package com.example.wallet.repository;

import com.example.wallet.model.implementations.DiscountCoupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DiscountCouponRepository extends JpaRepository<DiscountCoupon, Long> {

    Optional<DiscountCoupon> findByAccountClientIdAndCompanyAndExpirationDateAfter(
            Long accountClientId,
            String company,
            LocalDate expirationDate
    );

    List<DiscountCoupon> findByAccountClientIdAndExpirationDateAfter(
            Long accountClientId,
            LocalDate expirationDate
    );
}