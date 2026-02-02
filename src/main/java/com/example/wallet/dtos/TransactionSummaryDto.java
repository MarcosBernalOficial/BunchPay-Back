package com.example.wallet.dtos;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Map;

@Data
public class TransactionSummaryDto {
    private Integer month;
    private Integer year;
    private BigDecimal totalExpenses;
    private Map<String, BigDecimal> categoryTotals;
    private Map<String, Double> categoryPercentages;
}
