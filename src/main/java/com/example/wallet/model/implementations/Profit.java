package com.example.wallet.model.implementations;

import com.example.wallet.model.implementations.AccountClient;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "profits")
@Getter
@Setter
@NoArgsConstructor
public class Profit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "account_client_id", nullable = false)
    private AccountClient accountClient;

    private Double amount;

    private LocalDate date;
}

