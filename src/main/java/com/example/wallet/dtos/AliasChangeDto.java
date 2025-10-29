package com.example.wallet.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AliasChangeDto {
    @NotBlank(message = "Ingrese el nuevo Alias.")
    @Size(min = 6, max = 20, message = "El alias debe tener entre 6 y 20 caracteres.")
    private String newAlias;
}

