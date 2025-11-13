package com.example.wallet.services;

import com.example.wallet.model.implementations.Chat;
import com.example.wallet.model.implementations.Client;
import com.example.wallet.model.implementations.Support;
import com.example.wallet.repository.ChatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChatService {

    @SuppressWarnings("unused")
    @Autowired
    private MessageService messageService;

    private final NotificationService notificationService;
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    private ChatRepository chatRepository;

    public List<Chat> getChatsByClient(Client client) {
        return chatRepository.findByClient(client);
    }

    public List<Chat> getChatsBySupport(Support support) {
        return chatRepository.findBySupport(support);
    }

    public Optional<Chat> getChatById(Long id) {
        return chatRepository.findById(id);
    }

    public Chat saveChat(Chat chat) {
        return chatRepository.save(chat);
    }

    public void assignSupport(Chat chat, Support support) {
        chat.setSupport(support);
        chatRepository.save(chat);
    }

    public Optional<Chat> findUnassignedChatByClient(Client client) {
        return chatRepository.findByClient(client).stream()
                .filter(chat -> chat.getSupport() == null)
                .findFirst();
    }

    public List<Chat> getUnassignedChats() {
        return chatRepository.findAll().stream()
                .filter(chat -> chat.getSupport() == null)
                .filter(chat -> !chat.isClosed())
                .toList();
    }

    public void closeChat(Long chatId) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new RuntimeException("Chat no encontrado"));
        // Solo marcar como cerrado
        chat.setClosed(true);
        chatRepository.save(chat);

        System.out.println("Notificando al cliente: " + chat.getClient().getEmail());

        // Enviar notificación WebSocket al cliente
        messagingTemplate.convertAndSend("/topic/chats/" + chatId + "/closed", "Chat cerrado por el soporte");

        // Notificar al cliente
        notificationService.createNotification(
                chat.getClient(),
                "Chat de soporte cerrado",
                "El soporte ha cerrado tu chat. Si necesitás más ayuda, podés iniciar un nuevo chat desde tu cuenta.");
    }

    public Optional<Chat> findOpenChatByClient(Client client) {
        return chatRepository.findFirstByClientAndClosedFalseOrderByIdDesc(client);
    }

}
