package com.example.wallet.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PdfRequestDto {
    @NotBlank(message = "El contenido HTML es obligatorio.")
    private String html;

    @NotBlank(message = "El email es obligatorio.")
    @Email(message = "Debe ser un email válido.")
    private String email;
}


