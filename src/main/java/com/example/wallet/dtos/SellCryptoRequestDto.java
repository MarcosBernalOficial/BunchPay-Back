package com.example.wallet.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SellCryptoRequestDto {
    private String symbol;
    private Double amount; // Cantidad de crypto que quiere vender
}
