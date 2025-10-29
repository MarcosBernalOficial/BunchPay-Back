package com.example.wallet.controllers;

import com.example.wallet.dtos.RechargeRequestDto;
import com.example.wallet.services.RechargeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import jakarta.validation.Valid;

@RestController
@RequiredArgsConstructor
@Tag(name = "Servicios", description = "Gestiona el pago de servicios")
@RequestMapping("/api/recharge")
public class RechargeController {

    private final RechargeService rechargeService;

    @Operation(summary = "Cargar servicio", description = "Realiza la carga de dinero en un servicio")
    @PostMapping("/service")
    public ResponseEntity<String> processRecharge(@RequestBody @Valid RechargeRequestDto dto, Authentication auth) throws Exception {
        String email = auth.getName();
        rechargeService.processRecharge(dto, email);
        return ResponseEntity.ok("Recarga realizada");
    }
}
