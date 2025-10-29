package com.example.wallet.model.implementations;

import com.example.wallet.model.enums.TransactionType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime date;

    private Float amount;

    private String description;

    @Enumerated(EnumType.STRING)
    private TransactionType type;

    @ManyToOne
    @JoinColumn(name = "sender_client_id")
    private Client sender;

    @ManyToOne
    @JoinColumn(name = "reciever_client_id")
    private Client reciever;

    @ManyToOne
    @JoinColumn(name = "account_id")
    private AccountClient account;

    /* @ManyToOne
    @JoinColumn(name = "card_id")
    private Card card;
    */
}
