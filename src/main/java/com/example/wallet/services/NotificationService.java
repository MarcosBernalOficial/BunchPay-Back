package com.example.wallet.services;

import com.example.wallet.model.implementations.Client;
import com.example.wallet.model.implementations.Notification;
import com.example.wallet.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public void createNotification(Client client, String title, String message) {
        Notification notif = new Notification();
        notif.setClient(client);
        notif.setTitle(title);
        notif.setMessage(message);
        notif.setCreatedAt(LocalDateTime.now());
        notif.setRead(false);
        notificationRepository.save(notif);
    }

    public List<Notification> getNotificationsForClient(Long clientId) {
        return notificationRepository.findByClientIdOrderByCreatedAtDesc(clientId);
    }

    public void markAsRead(Long id) {
        Notification notif = notificationRepository.findById(id).orElseThrow();
        notif.setRead(true);
        notificationRepository.save(notif);
    }
}
