package com.example.wallet.controllers;

import com.example.wallet.dtos.LoginUserDto;
import com.example.wallet.dtos.RegisterUserDto;
import com.example.wallet.model.implementations.AccountClient;
import com.example.wallet.model.implementations.Client;
import com.example.wallet.model.implementations.User;
import com.example.wallet.security.JwtUtil;
import com.example.wallet.services.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Operaciones relacionadas al acceso y creacion de cuenta")
public class AuthController {


    private final AuthService authService;
    private final JwtUtil jwtUtil;

    @Operation(summary = "Registarse", description = "Recibe datos y crea una cuenta")
    @PostMapping("/register")
    public AccountClient register(@RequestBody @Valid RegisterUserDto request) {
        return authService.registerClient(request);
    }

    @Operation(summary = "Iniciar sesion", description = "Inicia sesion y devuelve un token")
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid LoginUserDto loginUserDto) {
        User user = authService.login(loginUserDto);
        String token = jwtUtil.generateToken(user.getEmail(), String.valueOf(user.getRole()));

        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("email", user.getEmail());
        response.put("nombre", user.getFirstName());
        response.put("apellido", user.getLastName());
        response.put("rol", user.getRole());

        if (user instanceof Client client) {
            response.put("dni", client.getDni());
        }

        // Si después tenés más datos que quieras devolver en base a roles, podés hacer lo mismo con Support o Admin

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Cerrar sesion", description = "Cierra la sesion de la cuenta")
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        return ResponseEntity.ok(Map.of("message", "Logout exitoso"));
    }

}
