package com.example.wallet.repository;

import com.example.wallet.model.implementations.Chat;
import com.example.wallet.model.implementations.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByChatOrderByDateAsc(Chat chat);
    List<Message> findByChat(Chat chat);

}

