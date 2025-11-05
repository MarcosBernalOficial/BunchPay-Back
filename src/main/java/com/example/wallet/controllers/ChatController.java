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
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/chats")
@RequiredArgsConstructor
@Tag(name = "Chats", description = "Gestiona las conversaciones entre el usuario y el soporte")
public class ChatController {

    private final ChatService chatService;
    private final MessageService messageService;
    private final ClientService clientService;
    private final SupportService supportService;
    private final SimpMessagingTemplate messagingTemplate;

    @Operation(summary = "Obtener chat", description = "Devuelve la lista de mensajes de un chat")
    @GetMapping("/{chatId}/messages")
    public ResponseEntity<List<Message>> getMessages(@PathVariable Long chatId) {
        Chat chat = chatService.getChatById(chatId).orElseThrow(() -> new RuntimeException("Chat no encontrado"));
        return ResponseEntity.ok(messageService.getMessagesByChat(chat));
    }

    @Operation(summary = "Enviar mensaje", description = "Envia un mensaje al chat")
    @PostMapping("/{chatId}/send")
    public ResponseEntity<Message> sendMessage(
            @PathVariable Long chatId,
            @RequestBody String content,
            Authentication auth) {
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

        Chat chat = chatService.getChatById(chatId).orElseThrow(() -> new RuntimeException("Chat no encontrado"));

        Message msg = messageService.sendMessage(chat, sender, content);
        return ResponseEntity.ok(msg);
    }

    // ===== Endpoints para soporte =====

    @Operation(summary = "Listar chats sin asignar")
    @GetMapping("/support/unassigned")
    public ResponseEntity<List<Map<String, Object>>> getUnassigned(Authentication auth) {
        try {
            // Verificar que sea soporte
            supportService.getByEmail(auth.getName());
        } catch (Exception e) {
            return ResponseEntity.status(403).build();
        }
        var chats = chatService.getUnassignedChats().stream().map(this::toSummary).collect(Collectors.toList());
        return ResponseEntity.ok(chats);
    }

    @Operation(summary = "Listar mis chats (soporte)")
    @GetMapping("/support/my")
    public ResponseEntity<List<Map<String, Object>>> getMyChats(Authentication auth) {
        try {
            var support = supportService.getByEmail(auth.getName());
            var chats = chatService.getChatsBySupport(support).stream().map(this::toSummary)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(chats);
        } catch (Exception e) {
            return ResponseEntity.status(403).build();
        }
    }

    @Operation(summary = "Asignarme un chat sin asignar")
    @PostMapping("/{chatId}/assign")
    public ResponseEntity<Map<String, Object>> assign(@PathVariable Long chatId, Authentication auth) {
        var support = supportService.getByEmail(auth.getName());
        var chat = chatService.getChatById(chatId).orElseThrow(() -> new RuntimeException("Chat no encontrado"));
        if (chat.getSupport() == null) {
            chatService.assignSupport(chat, support);
            // Notificar a soportes para refrescar listas
            messagingTemplate.convertAndSend("/topic/support/unassigned", java.util.Map.of(
                    "id", chat.getId(),
                    "status", "assigned"));
        }
        return ResponseEntity.ok(toSummary(chat));
    }

    @Operation(summary = "Cerrar chat")
    @PostMapping("/{chatId}/close")
    public ResponseEntity<Void> close(@PathVariable Long chatId, Authentication auth) {
        // puede validar si el soporte del chat es el mismo del token
        chatService.closeChat(chatId);
        // Notificar cierre para refrescar listados
        messagingTemplate.convertAndSend("/topic/support/unassigned", java.util.Map.of(
                "id", chatId,
                "status", "closed"));
        return ResponseEntity.ok().build();
    }

    private Map<String, Object> toSummary(Chat chat) {
        // Map.of no admite valores null y provoca NPE cuando support o client son null.
        java.util.Map<String, Object> summary = new java.util.HashMap<>();
        summary.put("id", chat.getId());
        summary.put("clientEmail", chat.getClient() != null ? chat.getClient().getEmail() : null);
        summary.put("clientName", chat.getClient() != null
                ? (chat.getClient().getFirstName() + " " + chat.getClient().getLastName())
                : null);
        summary.put("supportEmail", chat.getSupport() != null ? chat.getSupport().getEmail() : null);
        summary.put("closed", chat.isClosed());
        return summary;
    }
}
