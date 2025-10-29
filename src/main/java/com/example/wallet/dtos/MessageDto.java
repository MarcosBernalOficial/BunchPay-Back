package com.example.wallet.dtos;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class MessageDto {
    private Long id;
    private Long chatId;
    private String senderName;
    private String content;
    private Timestamp date;
    private String senderType;
}
