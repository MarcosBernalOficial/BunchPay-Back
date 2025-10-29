package com.example.wallet.dtos;

import com.example.wallet.model.enums.TransactionType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TransactionDto {
    private LocalDateTime date;
    private float amount;
    private String description;
    private TransactionType type;
    private String senderFirstName;
    private String senderLastName;
    private String senderCvu;
    private String recieverFirstName;
    private String recieverLastName;
    private String recieverCvu;
    private Long transactionId;
}
