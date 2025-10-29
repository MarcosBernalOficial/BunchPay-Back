package com.example.wallet.controllers;

import com.example.wallet.model.implementations.Chat;
import com.example.wallet.model.implementations.Message;
import com.example.wallet.model.implementations.User;
import com.example.wallet.services.ChatService;
import com.example.wallet.services.ClientService;
import com.example.wallet.services.MessageService;
import com.example.wallet.services.SupportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/chats")
@RequiredArgsConstructor
@Tag(name = "Chats", description = "Gestiona las conversaciones entre el usuario y el soporte")
public class ChatController {

    private final ChatService chatService;
    private final MessageService messageService;
    private final ClientService clientService;
    private final SupportService supportService;

    @Operation(summary = "Obtener chat", description = "Devuelve la lista de mensajes de un chat")
    @GetMapping("/{chatId}/messages")
    public ResponseEntity<List<Message>> getMessages(@PathVariable Long chatId) {
        Chat chat = chatService.getChatById(chatId).orElseThrow(() ->
                new RuntimeException("Chat no encontrado")
        );
        return ResponseEntity.ok(messageService.getMessagesByChat(chat));
    }

    @Operation(summary = "Enviar mensaje", description = "Envia un mensaje al chat")
    @PostMapping("/{chatId}/send")
    public ResponseEntity<Message> sendMessage(
            @PathVariable Long chatId,
            @RequestBody String content,
            Authentication auth
    ) {
        String email = auth.getName();
        User sender;

        // Buscar el usuario por email en ambos roles
        try {
            sender = clientService.getByEmail(email);
        } catch (Exception e1) {
            try {
                sender = supportService.getByEmail(email);
            } catch (Exception e2) {
                throw new RuntimeException("Usuario no encontrado.");
            }
        }

        Chat chat = chatService.getChatById(chatId).orElseThrow(() ->
                new RuntimeException("Chat no encontrado")
        );

        Message msg = messageService.sendMessage(chat, sender, content);
        return ResponseEntity.ok(msg);
    }
}
