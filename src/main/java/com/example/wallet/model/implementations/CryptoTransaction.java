package com.example.wallet.model.implementations;

import com.example.wallet.model.enums.CryptoTransactionType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "crypto_transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CryptoTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "account_client_id", nullable = false)
    private AccountClient accountClient;

    @Column(nullable = false)
    private String symbol; // BTC, ETH, USDT, etc.

    @Column(nullable = false)
    private Double amount; // Cantidad de crypto

    @Column(nullable = false)
    private Double priceArs; // Precio en ARS al momento de la transacción

    @Column(nullable = false)
    private Double totalArs; // Total en ARS (amount * priceArs)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CryptoTransactionType type; // BUY o SELL

    @Column(nullable = false)
    private LocalDateTime date;
}
