package com.example.wallet.dtos;

import lombok.Data;

import java.time.LocalDate;

@Data
public class CardDto {
    private String cardHolderName;
    private String cardNumber;
    private LocalDate expirationDate;
    private String maskedCardNumber; //To only show the last 4 numbers
    private String cvv;
}
