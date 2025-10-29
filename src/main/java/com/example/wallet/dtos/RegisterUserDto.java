package com.example.wallet.dtos;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterUserDto {
    @NotBlank(message = "El nombre no puede estar vacio")
    private String firstName;

    @NotBlank(message = "El lastName no puede estar vacio")
    private String lastName;

    @NotBlank(message = "El dni no puede estar vacio")
    @Pattern(regexp = "\\d{7,8}", message = "Debe ingresar un DNI valido")
    private String dni;

    @Email(message = "El tipo de mail no es valido")
    @NotBlank(message = "El email no puede estar vacio")
    private String email;

    @NotBlank(message = "La clave no puede estar vacia")
    private String password;
}
