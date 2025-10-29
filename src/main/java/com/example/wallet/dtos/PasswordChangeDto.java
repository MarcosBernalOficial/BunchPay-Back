package com.example.wallet.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PasswordChangeDto {
    @NotBlank(message = "Debe ingresar su contraseña actual.")
    private String currentPassword;

    @NotBlank(message = "Ingrese una nueva contraseña.")
    private String newPassword;
}
