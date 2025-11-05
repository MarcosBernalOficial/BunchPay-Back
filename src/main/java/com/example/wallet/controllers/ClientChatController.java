package com.example.wallet.controllers;

import com.example.wallet.dtos.ChatDto;
import com.example.wallet.dtos.MessageDto;
import com.example.wallet.model.implementations.Chat;
import com.example.wallet.model.implementations.Client;
import com.example.wallet.model.implementations.Message;
import com.example.wallet.model.implementations.Support;
import com.example.wallet.services.ChatService;
import com.example.wallet.services.ClientService;
import com.example.wallet.services.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/client/chats")
@RequiredArgsConstructor
@Tag(name = "Chats cliente", description = "Gestiona las conversaciones de un usuario")
public class ClientChatController {

    private final ChatService chatService;
    private final MessageService messageService;
    private final ClientService clientService;
    private final SimpMessagingTemplate messagingTemplate;

    @Operation(summary = "Obtener chats", description = "Devuelve la lista de chats")
    @GetMapping
    public ResponseEntity<List<ChatDto>> getMyChats(Authentication auth) {
        Client client = clientService.getByEmail(auth.getName());
        List<Chat> chats = chatService.getChatsByClient(client);

        List<ChatDto> dtos = chats.stream().map(chat -> {
            ChatDto dto = new ChatDto();
            dto.setId(chat.getId());
            dto.setClientName(chat.getClient().getFirstName());
            dto.setSupportName(chat.getSupport() != null ? chat.getSupport().getFirstName() : "No asignado");
            dto.setClosed(chat.isClosed());
            List<Message> messages = messageService.getMessagesByChat(chat);
            if (!messages.isEmpty()) {
                dto.setLastMessage(messages.get(messages.size() - 1).getContent());
            }
            return dto;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    @Operation(summary = "Obtener mensajes de un chat", description = "Devuelve la lista de mensajes de un chat")
    @GetMapping("/{chatId}/messages")
    public ResponseEntity<List<MessageDto>> getMessages(@PathVariable Long chatId, Authentication auth) {
        Chat chat = chatService.getChatById(chatId)
                .orElseThrow(() -> new RuntimeException("Chat no encontrado"));

        // Si el chat está cerrado, no devolver mensajes al cliente
        if (chat.isClosed()) {
            return ResponseEntity.ok(List.of()); // ← Lista vacía
        }

        List<Message> messages = messageService.getMessagesByChat(chat);
        List<MessageDto> dtos = messages.stream().map(this::toDto).toList();
        return ResponseEntity.ok(dtos);
    }

    @Operation(summary = "Enviar mensaje", description = "Envia un mensaje de un cliente al soporte")
    @PostMapping("/{chatId}/send")
    public ResponseEntity<MessageDto> sendMessage(@PathVariable Long chatId,
            @RequestBody String content,
            Authentication auth) {
        Client sender = clientService.getByEmail(auth.getName());
        Chat chat = chatService.getChatById(chatId)
                .orElseThrow(() -> new RuntimeException("Chat no encontrado"));
        if (chat.isClosed()) {
            return ResponseEntity.badRequest().body(null);
        }
        Message msg = messageService.sendMessage(chat, sender, content);
        return ResponseEntity.ok(toDto(msg));
    }

    private MessageDto toDto(Message m) {
        MessageDto dto = new MessageDto();
        dto.setId(m.getId());
        dto.setSenderName(m.getSender().getFirstName());
        dto.setContent(m.getContent());
        dto.setDate(m.getDate());
        dto.setSenderType(m.getSender() instanceof Support ? "SUPPORT" : "CLIENT");
        return dto;
    }

    @Operation(summary = "Iniciar chat", description = "Inicia una nueva conversacion con soporte")
    @PostMapping("/start")
    public ResponseEntity<ChatDto> startChat(Authentication auth) {
        Client client = clientService.getByEmail(auth.getName());

        Chat existing = chatService.findOpenChatByClient(client).orElse(null);
        if (existing != null) {
            ChatDto dto = new ChatDto();
            dto.setId(existing.getId());
            dto.setClientName(existing.getClient().getFirstName());
            dto.setSupportName(existing.getSupport() != null ? existing.getSupport().getFirstName() : "No asignado");
            dto.setClosed(existing.isClosed());
            return ResponseEntity.ok(dto);
        }

        // Crear el chat nuevo
        Chat newChat = new Chat();
        newChat.setClient(client);
        newChat.setSupport(null);
        newChat.setClosed(false);
        Chat saved = chatService.saveChat(newChat);

        // Devolver DTO
        ChatDto dto = new ChatDto();
        dto.setId(saved.getId());
        dto.setClientName(saved.getClient().getFirstName());
        dto.setSupportName("No asignado");
        dto.setClosed(saved.isClosed());

        // Notificar a soportes que hay un nuevo chat sin asignar
        messagingTemplate.convertAndSend("/topic/support/unassigned", java.util.Map.of(
                "id", saved.getId(),
                "clientName", saved.getClient() != null ? saved.getClient().getFirstName() : "",
                "status", "created"));
        return ResponseEntity.ok(dto);
    }

}
