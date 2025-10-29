package com.example.wallet.model.implementations;

import com.example.wallet.model.enums.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class User {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    protected Long id;

    protected String firstName;
    protected String lastName;

    @Column(unique = true, nullable = false)
    protected String email;

    protected String password;

    @Enumerated(EnumType.STRING)
    protected Role role;
}
