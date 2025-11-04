package com.example.wallet.controllers;

import com.example.wallet.model.implementations.Chat;
import com.example.wallet.model.implementations.Message;
import com.example.wallet.model.implementations.User;
import com.example.wallet.services.ChatService;
import com.example.wallet.services.ClientService;
import com.example.wallet.services.MessageService;
import com.example.wallet.services.SupportService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class ChatWsController {

    private final ChatService chatService;
    private final MessageService messageService;
    private final ClientService clientService;
    private final SupportService supportService;
    private final SimpMessagingTemplate messagingTemplate;

    public record InboundMessage(String content) {
    }

    @MessageMapping("/chats/{chatId}/send")
    public void handleChatMessage(@DestinationVariable Long chatId, InboundMessage inbound, Authentication auth) {
        if (auth == null || auth.getName() == null) {
            return; // o lanzar excepción si preferís
        }

        String email = auth.getName();
        User sender;
        try {
            sender = clientService.getByEmail(email);
        } catch (Exception e1) {
            sender = supportService.getByEmail(email);
        }

        Chat chat = chatService.getChatById(chatId)
                .orElseThrow(() -> new RuntimeException("Chat no encontrado"));

        Message saved = messageService.sendMessage(chat, sender, inbound.content());

        Map<String, Object> payload = new HashMap<>();
        payload.put("id", saved.getId());
        payload.put("chatId", chatId);
        payload.put("content", saved.getContent());
        payload.put("date", saved.getDate() != null ? saved.getDate().toInstant() : Instant.now());
        payload.put("senderEmail", email);
        payload.put("senderRole", sender.getRole().name());

        // Broadcast a todos los suscriptos al chat
        messagingTemplate.convertAndSend("/topic/chats/" + chatId, payload);
    }
}
