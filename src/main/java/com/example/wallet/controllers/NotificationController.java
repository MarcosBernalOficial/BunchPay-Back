package com.example.wallet.controllers;

import com.example.wallet.dtos.NotificationDto;
import com.example.wallet.model.implementations.Client;
import com.example.wallet.model.implementations.Notification;
import com.example.wallet.services.ClientService;
import com.example.wallet.services.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@Tag(name = "Notificaciones", description = "Gestiona las notificaciones de la cuenta")
public class NotificationController {

    private final NotificationService notificationService;
    private final ClientService clientService;

    @Operation(summary = "Obtener notificaciones", description = "Devuelve las notificaciones del cliente")
    @GetMapping
    public List<NotificationDto> getMyNotifications(Authentication auth) {
        Client client = clientService.getByEmail(auth.getName());
        List<Notification> notifications = notificationService.getNotificationsForClient(client.getId());
        return notifications.stream().map(this::mapToDto).toList();
    }

    private NotificationDto mapToDto(Notification n) {
        NotificationDto dto = new NotificationDto();
        dto.setId(n.getId());
        dto.setTitle(n.getTitle());
        dto.setMessage(n.getMessage());
        dto.setCreatedAt(n.getCreatedAt());
        dto.setRead(n.isRead());
        return dto;
    }

    @Operation(summary = "Marcar como leida", description = "Marca una notificacion como leida")
    @PutMapping("/{id}/read")
    public void markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
    }
}


