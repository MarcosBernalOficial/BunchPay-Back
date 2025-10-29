package com.example.wallet.dtos;

import com.example.wallet.model.enums.TransactionType;
import lombok.Data;

@Data

public class TransactionFilterDto {
    private TransactionType type;
    private Integer month;
}
