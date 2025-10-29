package com.example.wallet.controllers;

import com.example.wallet.controllers.exceptions.UnauthorizedAccessException;
import com.example.wallet.dtos.SupportProfileDto;
import com.example.wallet.model.enums.Role;
import com.example.wallet.model.implementations.Support;
import com.example.wallet.services.SupportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/support")
@RequiredArgsConstructor
@Tag(name = "Soportes", description = "Gestiona las cuentas de soporte")
public class SupportController {

    private final SupportService supportService;

    // Ver perfil del soporte autenticado
    @Operation(summary = "Datos de la cuenta", description = "Devuelve los datos de la cuenta del soporte")
    @GetMapping("/me")
    public ResponseEntity<SupportProfileDto> getOwnProfile(Authentication auth) {
        String email = auth.getName();
        Support support = supportService.getByEmail(email);
        return ResponseEntity.ok(toDto(support));
    }

    // Ver todos los soportes (solo ADMIN)
    @Operation(summary = "Obtener cuentas de soporte", description = "Devuelve todas las cuentas de soporte")
    @GetMapping("/all")
    public ResponseEntity<List<SupportProfileDto>> getAllSupports(Authentication auth) {
        Support current = supportService.getByEmail(auth.getName());
        if (current.getRole() != Role.ADMIN) {
            throw new UnauthorizedAccessException("No tenés permisos para ver esta información.");
        }

        List<SupportProfileDto> supports = supportService.getAllSupports().stream()
                .map(this::toDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(supports);
    }

    // Crear soporte (solo ADMIN)
    @Operation(summary = "Crear cuenta de soporte", description = "Crea una cuenta de soporte")
    @PostMapping("/create")
    public ResponseEntity<SupportProfileDto> createSupport(Authentication auth,
                                                           @RequestBody Support newSupport) {
        Support current = supportService.getByEmail(auth.getName());
        if (current.getRole() != Role.ADMIN) {
            throw new UnauthorizedAccessException("Solo un administrador puede crear cuentas soporte.");
        }

        Support created = supportService.save(newSupport);
        return ResponseEntity.ok(toDto(created));
    }

    @Operation(summary = "Modificar cuentas de soporte", description = "Modifica los datos de una cuenta de soporte")
    @PutMapping("/{id}")
    public ResponseEntity<SupportProfileDto> updateSupport(@PathVariable Long id,
                                                           @RequestBody SupportProfileDto dto,
                                                           Authentication auth) {
        Support current = supportService.getByEmail(auth.getName());

        if (current.getRole() != Role.ADMIN) {
            throw new UnauthorizedAccessException("Solo un administrador puede modificar cuentas soporte.");
        }

        Support updated = supportService.updateSupport(id, dto);
        return ResponseEntity.ok(toDto(updated));
    }



    // Eliminar soporte (solo ADMIN)
    @Operation(summary = "Eliminar cuentas de soporte", description = "Elimina un cuenta de soporte")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSupport(Authentication auth, @PathVariable Long id) {
        Support current = supportService.getByEmail(auth.getName());
        if (current.getRole() != Role.ADMIN) {
            throw new UnauthorizedAccessException("Solo un administrador puede eliminar cuentas soporte.");
        }

        supportService.deleteSupportById(id);
        return ResponseEntity.ok("Soporte eliminado con éxito");
    }

    private SupportProfileDto toDto(Support support) {
        SupportProfileDto dto = new SupportProfileDto();
        dto.setId(support.getId());
        dto.setFirstName(support.getFirstName());
        dto.setLastName(support.getLastName());
        dto.setEmail(support.getEmail());
        dto.setRole(support.getRole());
        return dto;
    }
}
