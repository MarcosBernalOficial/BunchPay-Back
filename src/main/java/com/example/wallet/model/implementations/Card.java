package com.example.wallet.model.implementations;

import com.example.wallet.model.enums.CardType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "card")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Card {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String identifier;
    private String bank;
    private String ownerName;

    @Enumerated(EnumType.STRING)
    private CardType type;

    private LocalDate validFrom;
    private LocalDate expirationDate;
    private String cvv;

    @ManyToOne
    @JoinColumn(name = "client_id")
    private Client client;
}
