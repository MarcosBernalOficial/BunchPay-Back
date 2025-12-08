package com.example.wallet.model.implementations;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "crypto_holdings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CryptoHolding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "account_client_id", nullable = false)
    private AccountClient accountClient;

    @Column(nullable = false)
    private String symbol; // BTC, ETH, USDT, etc.

    @Column(nullable = false)
    private Double amount; // Cantidad de crypto que posee

    @Column(nullable = false)
    private Double averagePurchasePrice; // Precio promedio de compra en ARS

    @Column(nullable = false)
    private LocalDateTime lastUpdated;
}
