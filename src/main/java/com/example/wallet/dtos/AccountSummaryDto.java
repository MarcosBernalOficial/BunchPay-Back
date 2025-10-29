package com.example.wallet.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountSummaryDto {
    private float balance;
    private String alias;
    private String cvu;
}
