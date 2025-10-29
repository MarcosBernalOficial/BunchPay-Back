package com.example.wallet.controllers;

import com.example.wallet.dtos.ClientProfileDto;
import com.example.wallet.dtos.PasswordChangeDto;
import com.example.wallet.dtos.UserUpdateDto;
import com.example.wallet.model.implementations.Client;
import com.example.wallet.repository.ClientRepository;
import com.example.wallet.services.ClientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import java.util.List;

@RestController
@RequestMapping("/client")
@Tag(name = "Cuentas de usuario", description = "Gestiona la cuenta de los usuarios")
public class ClientController {
    private final ClientService clientService;
    private final ClientRepository clientRepository;

    public ClientController(ClientRepository clientRepository, ClientService clientService){
        this.clientRepository = clientRepository;
        this.clientService = clientService;
    }

    @Operation(summary = "Obtener clientes", description = "Devuelve las cuentas de los clientes")
    @GetMapping("/all")
    public List<Client> viewAllClients(){
        return clientRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Client> searchForId(@PathVariable Long id){
        return  clientRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Datos de cliente", description = "Devuelve los datos de la cuenta del cliente")
    @GetMapping("/profile")
    public ResponseEntity<ClientProfileDto> viewProfile (Authentication auth) {
        String email = auth.getName(); // Extracts the email from the authenticated user
        ClientProfileDto profile = clientService.getProfile(email);
        return ResponseEntity.ok(profile);
    }

    @Operation(summary = "Modificar perfil del cliente", description = "Modifica los datos de la cuenta del cliente")
    @PutMapping("/profile")
    public ResponseEntity<?> updateMyProfile(@Valid @RequestBody UserUpdateDto dto, Authentication auth){
        clientService.updateProfile(auth.getName(), dto);
        return ResponseEntity.ok("Perfil actualizado correctamente.");
    }

    @Operation(summary = "Modificar clave del cliente", description = "Modifica la clave del cliente")
    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(@Valid @RequestBody PasswordChangeDto dto, Authentication auth){
        System.out.println("RECIBIDO: " + dto);
        clientService.changePassword(auth.getName(), dto);
        return ResponseEntity.ok("Contraseña modificada con exito.");
    }
}
