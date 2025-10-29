package com.example.wallet.model.implementations;

import com.example.wallet.model.enums.CardType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "support")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Support extends User {

    @OneToMany(mappedBy = "support", cascade = CascadeType.ALL)
    private List<Chat> chats;
}
