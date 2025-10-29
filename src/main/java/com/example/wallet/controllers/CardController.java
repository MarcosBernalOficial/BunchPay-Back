package com.example.wallet.controllers;

import com.example.wallet.dtos.CardDto;
import com.example.wallet.services.CardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cards")
@Tag(name = "Card", description = "Operaciones relacionadas a tarjeta del cliente")
public class CardController {

    private final CardService cardService;

    public CardController(CardService cardService){
        this.cardService = cardService;
    }

    @Operation(summary = "Obtener tarjeta", description = "Devuelve los datos de la tarjeta del cliente")
    @GetMapping("/my-card")
    public ResponseEntity<CardDto> getMyCard(Authentication auth){
        CardDto dto = cardService.getOneCard(auth.getName());

        return ResponseEntity.ok(dto);
    }
}
