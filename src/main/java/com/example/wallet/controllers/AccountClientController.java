package com.example.wallet.controllers;

import com.example.wallet.dtos.AccountSummaryDto;
import com.example.wallet.dtos.AliasChangeDto;
import com.example.wallet.repository.AccountClientRepository;
import com.example.wallet.services.AccountClientService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.Map;

@RestController
@RequestMapping("/accountClient")
@Tag(name = "AccountClient", description = "Operaciones relacionadas a la cuenta del usuario")
public class AccountClientController {
    private final AccountClientService accountClientService;
    @SuppressWarnings("unused")
    private final AccountClientRepository accountClientRepository;

    public AccountClientController(AccountClientRepository accountClientRepository,
            AccountClientService accountClientService) {
        this.accountClientRepository = accountClientRepository;
        this.accountClientService = accountClientService;
    }

    @Operation(summary = "Obtener balance", description = "Devuelve el balance de la cuenta")
    @GetMapping("/balance")
    public ResponseEntity<Map<String, Float>> viewBalance(Authentication auth) {
        String email = auth.getName();
        float balance = accountClientService.viewBalance(email);
        return ResponseEntity.ok(Map.of("balance", balance));
    }

    @Operation(summary = "Obtener alias", description = "Devuelve el alias de la cuenta")
    @GetMapping("/alias")
    public ResponseEntity<String> viewAlias(Authentication auth) {
        String email = auth.getName();
        String alias = accountClientService.getAlias(email);
        return ResponseEntity.ok(alias);
    }

    @Operation(summary = "Modificar alias", description = "Modifica el alias de la cuenta")
    @PatchMapping("/alias")
    public ResponseEntity<String> changeAlias(
            Authentication auth,
            @Valid @RequestBody AliasChangeDto dto) {
        String email = auth.getName(); // Esto toma el email que pusiste como principal en el filtro
        accountClientService.changeAlias(email, dto);
        return ResponseEntity.ok("Alias actualizado correctamente.");
    }

    @Operation(summary = "Obtener cvu", description = "Devuelve el cvu de la cuenta")
    @GetMapping("/cvu")
    public ResponseEntity<String> viewCVU(Authentication auth) {
        String email = auth.getName();
        String cvu = accountClientService.getCvu(email);
        return ResponseEntity.ok(cvu);
    }

    @Operation(summary = "Obtener resumen", description = "Devuelve el alias, cvu y balance de la cuenta")
    @GetMapping("/summary")
    public ResponseEntity<AccountSummaryDto> viewSummary(Authentication auth) {
        String email = auth.getName();
        AccountSummaryDto accountSummaryDto = accountClientService.getAccountSummary(email);
        return ResponseEntity.ok(accountSummaryDto);
    }

}