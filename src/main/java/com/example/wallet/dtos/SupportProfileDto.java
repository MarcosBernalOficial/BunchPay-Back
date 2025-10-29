package com.example.wallet.dtos;

import com.example.wallet.model.enums.Role;
import lombok.Data;

@Data
public class SupportProfileDto extends UserProfileDto{
    private Long id;
    private String firstName;
    private String lastName;
    private String password;
    private Role role;
}
