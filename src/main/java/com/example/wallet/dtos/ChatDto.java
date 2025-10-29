package com.example.wallet.dtos;

import lombok.Data;

@Data
public class ChatDto {
    private Long id;
    private String clientName;
    private String supportName;
    private String lastMessage;
    private boolean closed;
}
