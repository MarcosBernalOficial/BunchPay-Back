package com.example.wallet.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CryptoHoldingDto {
    private Long id;
    private String symbol;
    private Double amount;
    private Double averagePurchasePrice;
    private Double currentPrice;
    private Double totalValueArs;
    private Double profitLossArs;
    private Double profitLossPercentage;
}
