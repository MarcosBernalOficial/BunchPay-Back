package com.example.wallet.model.implementations;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "service")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Service {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String company;
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Transaction> paymentHistory;
    private String accountNumber;
}
