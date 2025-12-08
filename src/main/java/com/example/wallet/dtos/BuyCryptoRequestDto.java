package com.example.wallet.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BuyCryptoRequestDto {
    private String symbol;
    private Double amountArs; // Monto en ARS que quiere invertir
}
