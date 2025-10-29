package com.example.wallet.repository;

import com.example.wallet.model.implementations.Profit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ProfitRepository extends JpaRepository<Profit, Long> {

    List<Profit> findByAccountClientId(Long accountClientId);

    @Query("SELECT SUM(p.amount) FROM Profit p WHERE p.accountClient.id = :accountClientId")
    Double sumTotalProfits(@Param("accountClientId") Long accountClientId);

    @Query("SELECT SUM(p.amount) FROM Profit p WHERE p.accountClient.id = :accountClientId AND p.date = :date")
    Double sumTodayProfit(@Param("accountClientId") Long accountClientId, @Param("date") LocalDate date);
}
