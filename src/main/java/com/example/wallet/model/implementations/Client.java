package com.example.wallet.model.implementations;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "client")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Client extends User {

    @Column(unique = true, nullable = false)
    private String dni;

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL)
    private List<Chat> chats;
}
