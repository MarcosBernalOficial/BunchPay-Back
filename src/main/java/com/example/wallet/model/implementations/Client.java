package com.example.wallet.model.implementations;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "client")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
public class Client extends User {

    @Column(unique = true, nullable = false)
    private String dni;

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL)
    private List<Chat> chats;
}
