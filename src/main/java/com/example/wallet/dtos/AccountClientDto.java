package com.example.wallet.dtos;

import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountClientDto {
    @PositiveOrZero(message = "El balance debe ser cero o positivo")
    private float balance;
}
