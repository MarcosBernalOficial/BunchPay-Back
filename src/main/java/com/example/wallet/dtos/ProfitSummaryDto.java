package com.example.wallet.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfitSummaryDto {

    private Double todayProfit;
    private Double totalProfit;
}
