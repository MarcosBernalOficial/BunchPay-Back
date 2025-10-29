package com.example.wallet.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginUserDto {
    public String email;
    public String password;
}