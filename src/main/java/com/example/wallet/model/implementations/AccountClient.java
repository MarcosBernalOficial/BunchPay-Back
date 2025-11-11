package com.example.wallet.model.implementations;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "account_client")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AccountClient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "client_id")
    private Client client;

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Transaction> transactionsList;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Service> servicesList;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Card> cardList;

    private float balance;

    @Column(unique = true, nullable = false)
    private String alias;

    @Column(unique = true, nullable = false)
    private String cvu;
}
