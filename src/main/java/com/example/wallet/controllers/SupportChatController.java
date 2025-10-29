package com.example.wallet.controllers;

import com.example.wallet.dtos.ChatDto;
import com.example.wallet.dtos.MessageDto;
import com.example.wallet.model.implementations.Chat;
import com.example.wallet.model.implementations.Message;
import com.example.wallet.model.implementations.Support;
import com.example.wallet.services.ChatService;
import com.example.wallet.services.MessageService;
import com.example.wallet.services.SupportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/support/chats")
@RequiredArgsConstructor
@Tag(name = "Chats soporte", description = "Gestiona las conversaciones de un soporte")
public class SupportChatController {

    private final ChatService chatService;
    private final MessageService messageService;
    private final SupportService supportService;

    @Operation(summary = "Obtener chats", description = "Devuelve la lista de chats")
    @GetMapping
    public ResponseEntity<List<ChatDto>> getMyAssignedChats(Authentication auth) {
        Support support = supportService.getByEmail(auth.getName());
        List<Chat> chats = chatService.getChatsBySupport(support);

        List<ChatDto> dtos = chats.stream().map(chat -> {
            ChatDto dto = new ChatDto();
            dto.setId(chat.getId());
            dto.setClientName(chat.getClient().getFirstName());
            dto.setSupportName(support.getFirstName());

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
        Chat chat = chatService.getChatById(chatId).orElseThrow(() -> new RuntimeException("Chat no encontrado"));
        List<Message> messages = messageService.getMessagesByChat(chat);
        List<MessageDto> dtos = messages.stream().map(this::toDto).toList();
        return ResponseEntity.ok(dtos);
    }

    @Operation(summary = "Enviar mensaje", description = "Envia un mensaje de un cliente al soporte")
    @PostMapping("/{chatId}/send")
    public ResponseEntity<MessageDto> sendMessage(@PathVariable Long chatId,
                                                  @RequestBody String content,
                                                  Authentication auth) {
        Support sender = supportService.getByEmail(auth.getName());
        Chat chat = chatService.getChatById(chatId).orElseThrow(() -> new RuntimeException("Chat no encontrado"));
        Message msg = messageService.sendMessage(chat, sender, content);
        if (chat.isClosed()) {
            return ResponseEntity.badRequest().body(null);
        }
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

    @Operation(summary = "Obtener chats sin atender", description = "Devuelve los chats que se encuentran sin atender")
    @GetMapping("/unassigned")
    public ResponseEntity<List<ChatDto>> getUnassignedChats(Authentication auth) {
        Support support = supportService.getByEmail(auth.getName());

        List<Chat> chats = chatService.getUnassignedChats();
        List<ChatDto> dtos = chats.stream().map(chat -> {
            ChatDto dto = new ChatDto();
            dto.setId(chat.getId());
            dto.setClientName(chat.getClient().getFirstName());
            dto.setSupportName("No asignado");
            dto.setClosed(chat.isClosed());
            List<Message> messages = messageService.getMessagesByChat(chat);
            if (!messages.isEmpty()) {
                dto.setLastMessage(messages.get(messages.size() - 1).getContent());
            }
            return dto;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    @Operation(summary = "Asignar chat", description = "Un soporte se asigna una conversacion para atender a un cliente")
    @PutMapping("/{chatId}/assign")
    public ResponseEntity<?> assignChat(@PathVariable Long chatId, Authentication auth) {
        Support support = supportService.getByEmail(auth.getName());
        Chat chat = chatService.getChatById(chatId)
                .orElseThrow(() -> new RuntimeException("Chat no encontrado"));

        if (chat.getSupport() != null) {
            return ResponseEntity.badRequest().body("Este chat ya está asignado.");
        }

        chatService.assignSupport(chat, support);
        return ResponseEntity.ok("Chat asignado correctamente a " + support.getFirstName());
    }

    @Operation(summary = "Cerrar chat", description = "Un soporte finaliza un chat cuando se soluciona el problema")
    @PutMapping("/{chatId}/close")
    public ResponseEntity<?> closeChat(@PathVariable Long chatId, Authentication auth) {
        Support support = supportService.getByEmail(auth.getName());
        Chat chat = chatService.getChatById(chatId)
                .orElseThrow(() -> new RuntimeException("Chat no encontrado"));

        if (!chat.getSupport().getId().equals(support.getId())) {
            return ResponseEntity.status(403).body("No estás asignado a este chat.");
        }

        if (chat.isClosed()) {
            return ResponseEntity.badRequest().body("Este chat ya está cerrado.");
        }

        chatService.closeChat(chatId);
        return ResponseEntity.ok("Chat cerrado correctamente.");
    }

}
