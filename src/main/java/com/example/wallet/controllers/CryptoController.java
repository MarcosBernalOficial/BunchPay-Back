package com.example.wallet.controllers;

import com.example.wallet.controllers.exceptions.UnauthorizedAccessException;
import com.example.wallet.dtos.*;
import com.example.wallet.model.implementations.AccountClient;
import com.example.wallet.repository.AccountClientRepository;
import com.example.wallet.services.BinanceService;
import com.example.wallet.services.CryptoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/crypto")
@Tag(name = "Cripto", description = "Gestiona la compra, venta y visualización de criptomonedas")
public class CryptoController {
    private final BinanceService binanceService;
    private final CryptoService cryptoService;
    private final AccountClientRepository accountClientRepository;

    @Operation(summary = "Obtener cotizaciones", description = "Devuelve la cotizacion de las principales monedas en pesos")
    @GetMapping("/prices")
    public List<CryptoPriceDto> getPrices() {
        return binanceService.getAllPrices()
                .orElseThrow(() -> new RuntimeException("No se pudieron obtener los precios"));
    }

    @Operation(summary = "Comprar criptomoneda", description = "Compra criptomoneda con el saldo disponible")
    @PostMapping("/buy")
    public ResponseEntity<CryptoTransactionDto> buyCrypto(@RequestBody BuyCryptoRequestDto request,
            Authentication auth) {
        String email = auth.getName();
        AccountClient account = accountClientRepository.findByClientEmail(email)
                .orElseThrow(() -> new UnauthorizedAccessException("Sesión expirada"));

        CryptoTransactionDto transaction = cryptoService.buyCrypto(account, request);
        return ResponseEntity.ok(transaction);
    }

    @Operation(summary = "Vender criptomoneda", description = "Vende criptomoneda y acredita el dinero al saldo")
    @PostMapping("/sell")
    public ResponseEntity<CryptoTransactionDto> sellCrypto(@RequestBody SellCryptoRequestDto request,
            Authentication auth) {
        String email = auth.getName();
        AccountClient account = accountClientRepository.findByClientEmail(email)
                .orElseThrow(() -> new UnauthorizedAccessException("Sesión expirada"));

        CryptoTransactionDto transaction = cryptoService.sellCrypto(account, request);
        return ResponseEntity.ok(transaction);
    }

    @Operation(summary = "Ver mis criptomonedas", description = "Devuelve todas las criptomonedas que posee el usuario")
    @GetMapping("/holdings")
    public ResponseEntity<List<CryptoHoldingDto>> getMyHoldings(Authentication auth) {
        String email = auth.getName();
        AccountClient account = accountClientRepository.findByClientEmail(email)
                .orElseThrow(() -> new UnauthorizedAccessException("Sesión expirada"));

        List<CryptoHoldingDto> holdings = cryptoService.getMyHoldings(account.getId());
        return ResponseEntity.ok(holdings);
    }

    @Operation(summary = "Historial de transacciones crypto", description = "Devuelve el historial de compras y ventas")
    @GetMapping("/transactions")
    public ResponseEntity<List<CryptoTransactionDto>> getMyTransactions(Authentication auth) {
        String email = auth.getName();
        AccountClient account = accountClientRepository.findByClientEmail(email)
                .orElseThrow(() -> new UnauthorizedAccessException("Sesión expirada"));

        List<CryptoTransactionDto> transactions = cryptoService.getMyTransactions(account.getId());
        return ResponseEntity.ok(transactions);
    }
}
