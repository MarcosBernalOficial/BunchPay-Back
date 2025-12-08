package com.example.wallet.dtos;

import com.example.wallet.model.enums.CryptoTransactionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CryptoTransactionDto {
    private Long id;
    private String symbol;
    private Double amount;
    private Double priceArs;
    private Double totalArs;
    private CryptoTransactionType type;
    private LocalDateTime date;
}
