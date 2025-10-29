package com.example.wallet.services;

import com.example.wallet.model.enums.Role;
import com.example.wallet.model.implementations.Chat;
import com.example.wallet.model.implementations.Message;
import com.example.wallet.model.implementations.User;
import com.example.wallet.repository.MessageRepository;
import com.example.wallet.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageService {

    @Autowired
    private MessageRepository messageRepository;
    private NotificationService notificationService;

    public List<Message> getMessagesByChat(Chat chat) {
        return messageRepository.findByChatOrderByDateAsc(chat);
    }

    public void deleteMessagesByChat(Chat chat) {
        List<Message> messages = messageRepository.findByChat(chat);
        messageRepository.deleteAll(messages);
    }


    public Message sendMessage(Chat chat, User sender, String content) {
        Message message = new Message();
        message.setChat(chat);
        message.setSender(sender);
        message.setContent(content);
        message.setDate(Timestamp.from(Instant.now()));
        Message savedMessage = messageRepository.save(message);

        return savedMessage;
    }
}
