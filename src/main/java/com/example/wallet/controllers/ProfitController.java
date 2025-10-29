package com.example.wallet.controllers;

import com.example.wallet.dtos.ProfitSummaryDto;
import com.example.wallet.model.implementations.AccountClient;
import com.example.wallet.repository.AccountClientRepository;
import com.example.wallet.services.ProfitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/profits")
@RequiredArgsConstructor
@Tag(name = "Rendimiento", description = "Gestiona los rendimientos")
public class ProfitController {

    private final ProfitService profitService;
    private final AccountClientRepository accountClientRepository;

    @Operation(summary = "Obtener rendimiento", description = "Obtiene los rendimientos de la cuenta")
    @GetMapping("/summary")
    public ResponseEntity<ProfitSummaryDto> getSummary(Authentication auth) {
        String email = auth.getName();
        AccountClient acc = accountClientRepository.findByClientEmail(email).get();
        Long accountClientId = acc.getId();

        Double total = profitService.getTotalProfits(accountClientId);
        Double today = profitService.getTodayProfit(accountClientId);

        ProfitSummaryDto summary = new ProfitSummaryDto(today, total);
        return ResponseEntity.ok(summary);
    }
}

