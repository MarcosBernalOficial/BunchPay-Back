package com.example.wallet.repository;

import com.example.wallet.model.implementations.Chat;
import com.example.wallet.model.implementations.Client;
import com.example.wallet.model.implementations.Support;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatRepository extends JpaRepository<Chat, Long> {
    List<Chat> findByClient(Client client);
    List<Chat> findBySupport(Support support);
    Optional<Chat> findFirstByClientAndClosedFalseOrderByIdDesc(Client client);

}
